package com.lind.springbatch.entity;

import javax.validation.constraints.Size;

public class User {
  private Long id;
  @Size(max = 4, min = 2) //使用JSR-303注解来校验注解
  private String name;
  private int age;
  private String nation;
  private String address;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
