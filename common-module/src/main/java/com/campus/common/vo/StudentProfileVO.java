package com.campus.common.vo;

import lombok.Data;

@Data
public class StudentProfileVO {
    private String studentNo;
    private String name;
    private String gender;
    private String className;
    private String enrollYear;
    private String avatar;
    private String phone;
    private String guardianPhone;
}
