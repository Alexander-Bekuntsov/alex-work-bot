package ru.alex.tg.modules.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alex.tg.modules.repository.UserStateRepository;
import ru.alex.tg.modules.repository.domain.UserState;
import ru.alex.tg.modules.statemachine.FormEvents;
import ru.alex.tg.modules.statemachine.FormStates;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateMachineService {

    private final StateMachineFactory<FormStates, FormEvents> stateMachineFactory;
    private final UserStateRepository userStateRepository;

    @Transactional
    public void sendEvent(Long userId, FormEvents event) {
        StateMachine<FormStates, FormEvents> stateMachine = getStateMachine(userId);
        stateMachine.sendEvent(MessageBuilder.withPayload(event).build());
    }

    @Transactional
    public FormStates getCurrentState(Long userId) {
        StateMachine<FormStates, FormEvents> stateMachine = getStateMachine(userId);
        return stateMachine.getState().getId();
    }

    @Transactional
    public void saveFormData(Long userId, String key, String value) {
        UserState userState = userStateRepository.findById(userId)
                .orElseGet(() -> {
                    UserState newState = new UserState();
                    newState.setUserId(userId);
                    newState.setCurrentState(FormStates.IDLE.name());
                    return newState;
                });

        userState.getFormData().put(key, value);
        userStateRepository.save(userState);
    }

    @Transactional
    public String getFormData(Long userId, String key) {
        return userStateRepository.findById(userId)
                .map(state -> state.getFormData().get(key))
                .orElse(null);
    }

    @Transactional
    public void clearUserData(Long userId) {
        userStateRepository.deleteById(userId);
    }

    private StateMachine<FormStates, FormEvents> getStateMachine(Long userId) {
        StateMachine<FormStates, FormEvents> stateMachine = stateMachineFactory.getStateMachine(userId.toString());

        Optional<UserState> userStateOpt = userStateRepository.findById(userId);
        if (userStateOpt.isPresent()) {
            UserState userState = userStateOpt.get();
            resetStateMachineFromDb(stateMachine, userState);
        }

        stateMachine.getStateMachineAccessor()
                .withRegion()
                .addStateMachineInterceptor(new StateMachineInterceptorAdapter<FormStates, FormEvents>() {
                    @Override
                    public void postStateChange(State<FormStates, FormEvents> state, Message<FormEvents> message, Transition<FormStates, FormEvents> transition, StateMachine<FormStates, FormEvents> stateMachine, StateMachine<FormStates, FormEvents> rootStateMachine) {
                        saveStateMachine(userId, stateMachine);
                    }
                });

        return stateMachine;
    }

    private void saveStateMachine(Long userId, StateMachine<FormStates, FormEvents> stateMachine) {
        UserState userState = userStateRepository.findById(userId)
                .orElseGet(() -> {
                    UserState newState = new UserState();
                    newState.setUserId(userId);
                    return newState;
                });

        userState.setCurrentState(stateMachine.getState().getId().name());
        userStateRepository.save(userState);
    }

    private void resetStateMachineFromDb(StateMachine<FormStates, FormEvents> stateMachine, UserState userState) {
        stateMachine.stopReactively().block();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(accessor -> accessor.resetStateMachineReactively(
                        new DefaultStateMachineContext<>(
                                FormStates.valueOf(userState.getCurrentState()),
                                null, null, null)).block());
        stateMachine.startReactively().block();
    }
} 
