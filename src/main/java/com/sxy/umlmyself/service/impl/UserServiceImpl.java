package com.sxy.umlmyself.service.impl;

import com.sxy.umlmyself.dto.*;
import com.sxy.umlmyself.entity.*;
import com.sxy.umlmyself.repository.*;
import com.sxy.umlmyself.service.UserService;
import com.sxy.umlmyself.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * BCrypt 密码加密器
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ======================== 现有登录 / 确认角色方法保留 =====================

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // (原实现保持不变)
        // 1. 根据用户名查询用户信息
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名不存在"));

        // 2. 校验密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        List<Role> roles = roleRepository.findRolesByUserId(user.getUserId());
        if (roles == null || roles.isEmpty()) {
            throw new RuntimeException("该用户未分配任何角色");
        }

        List<RoleDTO> roleDTOs = roles.stream().map(role -> {
            RoleDTO dto = new RoleDTO();
            dto.setRoleId(role.getRoleId());
            dto.setRoleCode(role.getRoleCode());
            dto.setRoleName(role.getRoleName());
            return dto;
        }).collect(Collectors.toList());

        LoginResponseDTO resp = new LoginResponseDTO();
        resp.setUserId(user.getUserId());
        resp.setUsername(user.getUsername());
        resp.setRoles(roleDTOs);
        return resp;
    }

    @Override
    public ConfirmRoleResponseDTO confirmRole(ConfirmRoleRequestDTO confirmRoleRequest) {
        User user = userRepository.findById(confirmRoleRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        String roleCode = confirmRoleRequest.getRoleCode();
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new RuntimeException("未提供角色代码");
        }

        // 1) 先用 roleCode 查角色
        Role role = roleRepository.findByRoleCode(roleCode);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 2) 再校验该用户是否拥有该角色
        boolean hasRole = userRoleRepository.existsByUserIdAndRoleId(user.getUserId(), role.getRoleId());
        if (!hasRole) {
            throw new RuntimeException("该角色不属于该用户");
        }

        // 3) 生成 token
        String token = jwtUtil.generateToken(user.getUserId(), role.getRoleId());

        // 确保生成的token不包含非法字符（JWT字符串只能包含Base64URL字符）
        if (token != null) {
            token = token.trim();
            // 移除所有控制字符（ASCII 0-31）和删除字符（127）
            token = token.replaceAll("[\\x00-\\x1F\\x7F]", "");
            // 移除所有空白字符
            token = token.replaceAll("\\s+", "");
            // 只保留Base64URL字符和点号
            token = token.replaceAll("[^A-Za-z0-9\\-_.]", "");
        }

        ConfirmRoleResponseDTO resp = new ConfirmRoleResponseDTO();
        resp.setToken(token);
        resp.setUserId(user.getUserId());
        resp.setUsername(user.getUsername());
        resp.setRoleId(role.getRoleId());
        resp.setRoleCode(role.getRoleCode());
        return resp;
    }

    // ======================== 新增方法 =====================
    @Override
    public UserProfileDTO getCurrentUserProfile(Integer userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (!optUser.isPresent()) {
            return null;
        }
        User user = optUser.get();

        UserProfileDTO dto = new UserProfileDTO();
        dto.setAccount(user.getUsername());
        dto.setName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());

        // 角色代码
        List<Role> roles = roleRepository.findRolesByUserId(user.getUserId());
        if (!roles.isEmpty()) {
            dto.setRoleId(roles.get(0).getRoleCode()); // 取第一个角色
        }

        // 如果是学生，获取学院、专业、导师
        studentRepository.findByUser_UserIdWithCollegeAndMajor(user.getUserId()).ifPresent(stu -> {
            dto.setCollege(stu.getCollege() != null ? stu.getCollege().getCollegeName() : null);
            dto.setMajor(stu.getMajor() != null ? stu.getMajor().getMajorName() : null);
            if (stu.getTeaSupervisorId() != null) {
                teacherRepository.findByTeaId(stu.getTeaSupervisorId()).ifPresent(tea -> dto.setSupervisor(tea.getTeaName()));
            }
        });

        return dto;
    }

    @Override
    public void updateUserProfile(Integer userId, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setRealName(userUpdateDTO.getName());
        user.setPhone(userUpdateDTO.getPhone());
        user.setEmail(userUpdateDTO.getEmail());

        userRepository.save(user);
    }
}
