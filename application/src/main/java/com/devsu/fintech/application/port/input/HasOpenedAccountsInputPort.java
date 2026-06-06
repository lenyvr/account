package com.devsu.fintech.application.port.input;

public interface HasOpenedAccountsInputPort {

    boolean execute(Long clientId);
}
