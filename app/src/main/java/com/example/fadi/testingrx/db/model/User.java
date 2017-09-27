package com.example.fadi.testingrx.db.model;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

/**
 * Created by fadi on 27/09/2017.
 */

@Entity(nameInDb = "user")
public class User {

    @Id(autoincrement = true)
    private Long id;

    private String fireBaseUid;
    private String name;
    private Integer height;
    private Float weight;

    @Convert(converter = UserGenderConverter.class, columnType = String.class)
    private UserGender gender;

    public User() {
    }

    public User(String fireBaseUid, String name, Integer height, Float weight, UserGender gender, Date birthDate) {
        this.fireBaseUid = fireBaseUid;
        this.name = name;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    @Generated(hash = 322538420)
    public User(Long id, String fireBaseUid, String name, Integer height, Float weight, UserGender gender, Date birthDate) {
        this.id = id;
        this.fireBaseUid = fireBaseUid;
        this.name = name;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    public String getFireBaseUid() {
        return fireBaseUid;
    }

    public void setFireBaseUid(String fireBaseUid) {
        this.fireBaseUid = fireBaseUid;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public UserGender getGender() {
        return gender;
    }

    public void setGender(UserGender gender) {
        this.gender = gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private Date birthDate;




}
