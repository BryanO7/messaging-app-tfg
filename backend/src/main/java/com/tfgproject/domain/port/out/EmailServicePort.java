// domain/port/out/EmailServicePort.java
package com.tfgproject.domain.port.out;

import com.tfgproject.application.command.SendEmailCommand;

public interface EmailServicePort {
    boolean sendEmail(SendEmailCommand command);
}

