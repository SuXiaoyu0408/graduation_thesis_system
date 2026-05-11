package com.sxy.umlmyself.entity;

import lombok.Data;

import jakarta.persistence.*;

@Entity
@Table(name = "student")
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stu_id")
    private Integer stuId;

    @Column(name = "stu_name")
    private String stuName;

    @Column(name = "tea_supervisor_id")
    private Integer teaSupervisorId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "college_id", referencedColumnName = "college_id")
    private College college;

    @ManyToOne
    @JoinColumn(name = "major_id", referencedColumnName = "major_id")
    private Major major;
}

