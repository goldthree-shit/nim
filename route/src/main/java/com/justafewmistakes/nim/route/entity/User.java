package com.justafewmistakes.nim.route.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    @TableId("id")
    private Long id; //用户唯一id
    private String username;
    private String password;
    private int status;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;
}
