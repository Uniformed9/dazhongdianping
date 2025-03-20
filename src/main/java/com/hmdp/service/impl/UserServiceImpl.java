package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Random;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1校验手机号
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误");
        }
        //2不符合
        String code= RandomUtil.randomNumbers(6);

        //3符合生成验证码，
        //4保存验证码
        session.setAttribute("code",code);
        //
        log.debug("发送成功:{}",code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1校验
        String phone = loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号错误");
        }
        //2校验验证码
        Object cashCode=session.getAttribute("code");
        Object phoneCode=session.getAttribute("phoneCode");
        String code=loginForm.getCode();
        if(cashCode==null||!cashCode.toString().equals(code)){
            //3不一致报错
            return Result.fail("验证码错误");
        }
        //4一致查询手机号
        User user=query().eq("phone",phone).one();
        //5没有创建
        if(user==null){
            user=createUserWithPhone(phone);

        }
        BeanUtil.copyProperties(user, UserDTO.class);
        //6保存session
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        User user=new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(6));
        save(user);
        return user;
    }
}
