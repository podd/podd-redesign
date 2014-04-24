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

import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Extension of the default JUnit {@link Timeout} rule to print the stack traces of all active
 * threads when a timeout occurs.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class TimeoutWithStackTraces implements TestRule
{
    private final long fMillis;

    /**
     * @param millis
     *            the millisecond timeout
     */
    public TimeoutWithStackTraces(final long millis)
    {
        this.fMillis = millis;
    }

    @Override
    public Statement apply(final Statement base, final Description description)
    {
        return new FailOnTimeoutWithStackTraces(base, this.fMillis);
    }
}
