package ru.alex.tg.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import ru.alex.tg.modules.statemachine.FormEvents;
import ru.alex.tg.modules.statemachine.FormStates;

@Slf4j
@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<FormStates, FormEvents> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<FormStates, FormEvents> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<FormStates, FormEvents> states) throws Exception {
        states
                .withStates()
                .initial(FormStates.IDLE)
                .state(FormStates.WAITING_NAME)
                .state(FormStates.WAITING_EMAIL)
                .state(FormStates.WAITING_SCORE)
                .end(FormStates.COMPLETED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<FormStates, FormEvents> transitions) throws Exception {
        transitions
                .withExternal()
                .source(FormStates.IDLE).target(FormStates.WAITING_NAME)
                .event(FormEvents.START_FORM)
                .and()
                .withExternal()
                .source(FormStates.WAITING_NAME).target(FormStates.WAITING_EMAIL)
                .event(FormEvents.NAME_PROVIDED)
                .and()
                .withExternal()
                .source(FormStates.WAITING_EMAIL).target(FormStates.WAITING_SCORE)
                .event(FormEvents.EMAIL_PROVIDED)
                .and()
                .withExternal()
                .source(FormStates.WAITING_SCORE).target(FormStates.COMPLETED)
                .event(FormEvents.SCORE_PROVIDED)
                .and()
                .withExternal()
                .source(FormStates.COMPLETED).target(FormStates.IDLE)
                .event(FormEvents.RESET);
    }

    private StateMachineListener<FormStates, FormEvents> listener() {
        return new StateMachineListenerAdapter<FormStates, FormEvents>() {
            @Override
            public void stateChanged(State<FormStates, FormEvents> from, State<FormStates, FormEvents> to) {
                log.debug("State changed from {} to {}",
                        from != null ? from.getId() : "none",
                        to != null ? to.getId() : "none");
            }
        };
    }
} 
