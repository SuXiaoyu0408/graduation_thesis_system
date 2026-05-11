package com.sxy.umlmyself.converter;

import com.sxy.umlmyself.enums.MaterialType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToMaterialTypeConverter implements Converter<String, MaterialType> {

    @Override
    public MaterialType convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            return MaterialType.valueOf(source.toLowerCase());
        } catch (IllegalArgumentException e) {
            // You can log the error or handle it as needed
            // For example, return null or throw a more specific exception
            throw new IllegalArgumentException("Invalid value for MaterialType: " + source, e);
        }
    }
}

