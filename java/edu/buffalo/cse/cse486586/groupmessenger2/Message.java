package edu.buffalo.cse.cse486586.groupmessenger2;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by mohit on 3/9/15.
 */
public class Message implements Serializable {
    private String my_port;
    private String message;
    private String sender;
    private String identifier;
    private double proposal;
    private boolean deliverable;
    private boolean timeup;
    private int message_number;

    public int getMessage_number() {
        return message_number;
    }

    public void setMessage_number(int message_number) {
        this.message_number = message_number;
    }

    public boolean isTimeup() {
        return timeup;
    }

    public void setTimeup(boolean timeup) {
        this.timeup = timeup;
    }

    public boolean isDeliverable() {
        return deliverable;
    }

    public void setDeliverable(boolean deliverable) {
        this.deliverable = deliverable;
    }

    public double getProposal() {
        return proposal;
    }

    public void setProposal(double proposal) {
        this.proposal = proposal;
    }

    public String getMy_port() {
        return my_port;
    }

    public void setMy_port(String my_port) {
        this.my_port = my_port;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
