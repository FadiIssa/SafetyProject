package com.example.fadi.testingrx.db.model;
import org.greenrobot.greendao.converter.PropertyConverter;

public class UserGenderConverter implements PropertyConverter<UserGender, String> {
    @Override
    public UserGender convertToEntityProperty(String databaseValue) {
        return UserGender.valueOf(databaseValue);
    }

    @Override
    public String convertToDatabaseValue(UserGender entityProperty) {
        return entityProperty.name();
    }
}
