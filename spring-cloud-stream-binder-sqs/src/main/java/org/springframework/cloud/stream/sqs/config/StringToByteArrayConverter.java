package org.springframework.cloud.stream.sqs.config;

import org.springframework.core.convert.converter.Converter;

public class StringToByteArrayConverter implements Converter<String, byte[]> {

    @Override
    public byte[] convert(String source) {
        return source.getBytes();
    }
}
