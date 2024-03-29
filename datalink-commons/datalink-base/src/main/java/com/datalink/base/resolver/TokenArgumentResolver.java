package com.datalink.base.resolver;

import com.datalink.base.annotation.LoginUser;
import com.datalink.base.constant.SecurityConstant;
import com.datalink.base.feign.UserService;
import com.datalink.base.model.Role;
import com.datalink.base.model.User;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TokenArgumentResolver
 *
 * @author wenmo
 * @since 2021/5/10 20:51
 */
@Slf4j
public class TokenArgumentResolver implements HandlerMethodArgumentResolver {
    private UserService userService;

    public TokenArgumentResolver(UserService userService) {
        this.userService = userService;
    }

    /**
     * 入参筛选
     *
     * @param methodParameter 参数集合
     * @return 格式化后的参数
     */
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(LoginUser.class) && methodParameter.getParameterType().equals(User.class);
    }

    /**
     * @param methodParameter       入参集合
     * @param modelAndViewContainer model 和 view
     * @param nativeWebRequest      web相关
     * @param webDataBinderFactory  入参解析
     * @return 包装对象
     */
    @Override
    public Object resolveArgument(MethodParameter methodParameter,
                                  ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest,
                                  WebDataBinderFactory webDataBinderFactory) {
        LoginUser loginUser = methodParameter.getParameterAnnotation(LoginUser.class);
        boolean isFull = loginUser.isFull();
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        String userId = request.getHeader(SecurityConstant.USER_ID_HEADER);
        String username = request.getHeader(SecurityConstant.USER_HEADER);
        String roles = request.getHeader(SecurityConstant.ROLE_HEADER);
        if (StrUtil.isBlank(username)) {
            log.warn("resolveArgument error username is empty");
            return null;
        }
        User user;
        if (isFull) {
            user = userService.selectByUsername(username);
        } else {
            user = new User();
            user.setId(Integer.valueOf(userId));
            user.setUsername(username);
        }
        List<Role> roleList = new ArrayList<>();
        Arrays.stream(roles.split(",")).forEach(role -> {
            Role role1 = new Role();
            role1.setCode(role);
            roleList.add(role1);
        });
        user.setRoles(roleList);
        return user;
    }
}
