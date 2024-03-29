/*
 * The MIT License (MIT)
 *
 * Copyright 2022 Crown Copyright (Health Education England)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package uk.nhs.tis.sync.job;

import javax.annotation.Nullable;

/**
 * A Sync Job which can be run on demand. The Implementing class MAY use any provided parameters. It
 * has been created in response to automated code analysis, therefore should be reviewed if more
 * than one implementation will make use of the {@link RunnableJob#run(String)} method.
 */
public interface RunnableJob {

  boolean isCurrentlyRunning();

  /**
   * Wrapper method for running jobs with Nullable parameters.
   *
   * <p>N.B. If there is further use of this method, the String parameter should be replaced with a
   * better construct, e.g. `Map` or PropertySource.</p>
   *
   * @param params Parameters for running the job if this is supported. Can be null.
   */
  void run(@Nullable String params);
}
