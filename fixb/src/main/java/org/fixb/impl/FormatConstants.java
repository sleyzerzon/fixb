/*
 * Copyright 2013 YTEQ Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fixb.impl;

/**
 * Literals used in FIX message formatting, i.e. date/time formats, field delimiter.
 *
 * @author vladyslav.yatsenko
 */
public final class FormatConstants {
    public static final char SOH = 0x01;
    public static final String DATE = "yyyyMMdd";
    public static final String TIME = "HH:mm:ss";
    public static final String DATE_TIME = DATE + "-" + TIME;
    public static final String TIME_WITH_MILLIS = "HH:mm:ss.SSS";
    public static final String DATE_TIME_WITH_MILLIS = DATE + "-" + TIME_WITH_MILLIS;
    public static final String DATE_WITH_TZ = TIME + "Z";
    public static final String DATE_TIME_WITH_TZ = DATE_TIME + "Z";

    private FormatConstants() {
    }
}
