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

package org.fix4j.impl;

import org.fix4j.FixMessageBuilder;
import org.fix4j.meta.FixFieldMeta;
import org.fix4j.meta.FixGroupMeta;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * I implement FixMessageBuilder and I can build raw (represented as a String) FIX messages ready for transmission.
 * I am the fastest implementation as I don't create any intermediate object.
 *
 * @author vladyslav.yatsenko
 */
public final class NativeFixMessageBuilder extends FixMessageBuilder<String> {
    private final StringBuilder head = new StringBuilder();
    private final StringBuilder body;

    public static final class Factory implements FixMessageBuilder.Factory<String, NativeFixMessageBuilder> {

        @Override
        public NativeFixMessageBuilder create() {
            return new NativeFixMessageBuilder(new StringBuilder());
        }

        @Override
        public NativeFixMessageBuilder createWithMessage(String fixMessage) {
            return new NativeFixMessageBuilder(new StringBuilder(fixMessage));
        }
    }

    @Override
    public String build() {
        return appendTag(head.append(body), 10).append(generateCheckSum(head)).toString();
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, char value, boolean header) {
        appendTag(tag, header).append(value).append(FormatConstants.SOH);
        return this;
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, int value, boolean header) {
        appendTag(tag, header).append(value).append(FormatConstants.SOH);
        return this;
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, double value, boolean header) {
        appendTag(tag, header).append(value).append(FormatConstants.SOH);
        return this;
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, boolean value, boolean header) {
        appendTag(tag, header).append(value ? 'Y' : 'N').append(FormatConstants.SOH);
        return this;
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, BigDecimal value, boolean header) {
        return setField(tag, value.toString(), header);
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, LocalDate value, boolean header) {
        return setField(tag, value.toString(FormatConstants.DATE_FORMAT), header);
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, LocalTime value, boolean header) {
        final String pattern = (value.getMillisOfSecond() == 0) ? FormatConstants.TIME_FORMAT : FormatConstants.TIME_FORMAT_WITH_MILLIS;
        return setField(tag, value.toString(pattern), header);
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, LocalDateTime value, boolean header) {
        final String pattern = (value.getMillisOfSecond() == 0) ? FormatConstants.DATE_TIME_FORMAT : FormatConstants.DATE_TIME_FORMAT_WITH_MILLIS;
        return setField(tag, value.toString(pattern), header);
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, DateTime value, boolean header) {
        final String pattern = (value.getMillisOfSecond() == 0) ? FormatConstants.DATE_TIME_FORMAT : FormatConstants.DATE_TIME_FORMAT_WITH_MILLIS;
        return setField(tag, value.toString(pattern), header);
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, Instant value, boolean header) {
        final String pattern = (value.get(DateTimeFieldType.millisOfSecond()) == 0) ? FormatConstants.DATE_TIME_FORMAT : FormatConstants.DATE_TIME_FORMAT_WITH_MILLIS;
        return setField(tag, value.toString(DateTimeFormat.forPattern(pattern)), header);
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, Date value, boolean header) {
        final String pattern = (value.getTime() % 1000 == 0) ? FormatConstants.DATE_TIME_FORMAT : FormatConstants.DATE_TIME_FORMAT_WITH_MILLIS;
        return setField(tag, new SimpleDateFormat(pattern).format(value), header);
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, String value, boolean header) {
        appendTag(tag, header).append(value).append(FormatConstants.SOH);
        return this;
    }

    @Override
    public FixMessageBuilder<String> setField(int tag, Enum<?> value, boolean header) {
        return setField(tag, value.ordinal(), header);
    }

    @Override
    public FixMessageBuilder<String> setGroups(int tag, int componentTag, Collection<?> collection, boolean header) {
        if (!collection.isEmpty()) {
            setField(tag, collection.size(), header);
            for (Object o : collection) {
                setField(componentTag, o, header);
            }
        }
        return this;
    }

    @Override
    public FixMessageBuilder<String> setGroups(int tag, List<FixFieldMeta> fields, Collection<?> collection, boolean header) {
        if (!collection.isEmpty()) {
            setField(tag, collection.size(), header);
            for (Object o : collection) {
                for (FixFieldMeta fieldMeta : fields) {
                    if (fieldMeta.isGroup()) {
                        final FixGroupMeta groupMeta = (FixGroupMeta) fieldMeta;
                        if (groupMeta.isSimple()) {
                            setGroups(groupMeta.getTag(), groupMeta.getComponentTag(), (Collection<?>) o, header);
                        } else {
                            setGroups(groupMeta.getTag(), groupMeta.getComponentMeta().getFields(), (Collection<?>) o, header);
                        }
                    } else {
                        setField(fieldMeta.getTag(), fieldMeta.getValue(o), header);
                    }
                }
            }
        }
        return this;
    }

    private NativeFixMessageBuilder(final StringBuilder body) {
        this.body = body;
    }

    private StringBuilder appendTag(int tag, boolean header) {
        return appendTag((header ? head : body), tag);
    }

    private StringBuilder appendTag(final StringBuilder buf, int tag) {
        return buf.append(tag).append('=');
    }

    private int generateCheckSum(StringBuilder buf) {
        int sum = 0;
        for (int idx = 0; idx < buf.length(); idx++) {
            sum += buf.charAt(idx);
        }
        return sum % 256;
    }

}