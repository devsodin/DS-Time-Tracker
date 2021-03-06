package com.dstimetracker.devsodin.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Clock used for controlling time inside the project.
 * Observable Singleton.
 * It refreshes with a settable rate.
 */
public final class Clock extends Observable {

    public static final int MS_IN_SEC = 1000;
    static final Logger LOGGER = LoggerFactory.getLogger(Clock.class);
    public static int nREFRESHRATE = 1;
    private static Clock clock = null;
    private final Timer timer;
    private Date date;
    private TimerTask tt = null;

    /**
     * Private constructor. It creates the timer and sets the date.
     */
    private Clock() {
        this.timer = new Timer(true);
        this.date = new Date();
        setupTimer();
        Clock.LOGGER.info("Call to private Clock constructor. Clock created");
    }

    /**
     * The invoker of the class.
     * Whenever is called it returns the clock.
     * The first time, in addition of the return, it creates the clock first.
     *
     * @return the Clock instance
     */
    public static synchronized Clock getInstance() {
        if (Clock.clock == null) {
            Clock.clock = new Clock();
        }
        return Clock.clock;
    }

    /**
     * Set up the timer to refresh with the user Refresh
     * rate to call an update method with this
     * periodicity.
     */
    private void setupTimer() {
        this.timer.scheduleAtFixedRate(this.tt = new TimerTask() {
            @Override
            public void run() {
                updateClock();
            }
        }, 0, Clock.nREFRESHRATE * Clock.MS_IN_SEC);
    }


    /**
     * Updates his date with the actual date and notify all the observers.
     */
    private void updateClock() {
        this.setTime(new Date());
        setChanged();
        notifyObservers(this);
    }


    /**
     * Method to set up the refresh rate (checking min
     * value) and re-setup the clock with the new configuration.
     *
     * @param secs the seconds wanted for refresh rate
     */
    public void setRefreshTicks(final int secs) {
        if (secs >= 1) {
            Clock.LOGGER.info("The clock changed the nREFRESHRATE to " + secs);
            Clock.nREFRESHRATE = secs;
            if (this.timer != null) {
                this.tt.cancel();
                setupTimer();
            }
        } else {
            throw new IllegalArgumentException("Value needs to be 1 or above");
        }

    }

    /**
     * Method that interrupts the thread and cancel the task.
     */
    public void stopClock() {
        this.tt.cancel();
        this.timer.cancel();
        Clock.LOGGER.info("The clock has been stopped");

    }


    /**
     * Method to get the date in Date format.
     *
     * @return Date object with clock's date
     */
    public Date getTime() {
        return date;
    }

    /**
     * Setter for the actual time.
     *
     * @param time the actual time.
     */
    private void setTime(final Date time) {
        this.date = time;
    }

    /**
     * Method to get the date in ms (long).
     *
     * @return long with clock's date
     */
    public long getMs() {
        return clock.getTime().getTime();
    }
}
