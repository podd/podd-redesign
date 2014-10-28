/**
 * PODD is an OWL ontology database used for scientific project management
 *
 * Copyright (C) 2009-2013 The University Of Queensland
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.podd.junit.ext;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runners.model.Statement;

/**
 * Extension of the default JUnit {@link FailOnTimeout} statement to print the stack traces of all
 * active threads when a timeout occurs.
 *
 * Enhanced version of {@link FailOnTimeout}
 */
public class FailOnTimeoutWithStackTraces extends Statement
{
    private class CallableStatement implements Callable<Throwable>
    {
        @Override
        public Throwable call() throws Exception
        {
            try
            {
                FailOnTimeoutWithStackTraces.this.fOriginalStatement.evaluate();
            }
            catch(final Exception e)
            {
                throw e;
            }
            catch(final Throwable e)
            {
                return e;
            }
            return null;
        }
    }
    
    private final Statement fOriginalStatement;
    private final TimeUnit fTimeUnit;
    
    private final long fTimeout;
    
    public FailOnTimeoutWithStackTraces(final Statement originalStatement, final long millis)
    {
        this(originalStatement, millis, TimeUnit.MILLISECONDS);
    }
    
    public FailOnTimeoutWithStackTraces(final Statement originalStatement, final long timeout, final TimeUnit unit)
    {
        this.fOriginalStatement = originalStatement;
        this.fTimeout = timeout;
        this.fTimeUnit = unit;
    }
    
    private Exception createTimeoutException(final Thread thread)
    {
        final String allStackTraces = this.getStackTraces();
        
        Exception exception;
        if(allStackTraces.length() == 0)
        {
            exception =
                    new Exception(String.format("test timed out after %d %s", this.fTimeout, this.fTimeUnit.name()
                            .toLowerCase()));
        }
        else
        {
            exception =
                    new Exception(String.format(
                            "test timed out after %d %s\nAll threads active when test timeout occurred:\n %s",
                            this.fTimeout, this.fTimeUnit.name().toLowerCase(), allStackTraces));
        }
        final StackTraceElement[] stackTrace = thread.getStackTrace();
        if(stackTrace != null)
        {
            exception.setStackTrace(stackTrace);
            thread.interrupt();
        }
        return exception;
    }
    
    @Override
    public void evaluate() throws Throwable
    {
        final FutureTask<Throwable> task = new FutureTask<Throwable>(new CallableStatement());
        final Thread thread = new Thread(task, "Time-limited test");
        thread.setDaemon(true);
        thread.start();
        final Throwable throwable = this.getResult(task, thread);
        if(throwable != null)
        {
            throw throwable;
        }
    }
    
    /**
     * Wait for the test task, returning the exception thrown by the test if the test failed, an
     * exception indicating a timeout if the test timed out, or {@code null} if the test passed.
     */
    private Throwable getResult(final FutureTask<Throwable> task, final Thread thread)
    {
        try
        {
            return task.get(this.fTimeout, this.fTimeUnit);
        }
        catch(final InterruptedException e)
        {
            return e; // caller will re-throw; no need to call
            // Thread.interrupt()
        }
        catch(final ExecutionException e)
        {
            // test failed; have caller re-throw the exception thrown by the
            // test
            return e.getCause();
        }
        catch(final TimeoutException e)
        {
            return this.createTimeoutException(thread);
        }
    }
    
    /**
     * Gets all thread stack traces.
     *
     * @return string of all thread stack traces
     */
    private String getStackTraces()
    {
        final StringBuilder sb = new StringBuilder();
        final Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
        for(final Thread t : stacks.keySet())
        {
            sb.append(t.toString()).append('\n');
            for(final StackTraceElement ste : t.getStackTrace())
            {
                sb.append("\tat ").append(ste.toString()).append('\n');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
