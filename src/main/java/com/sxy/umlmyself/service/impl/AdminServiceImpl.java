package com.sxy.umlmyself.service.impl;

import com.sxy.umlmyself.common.BusinessException;
import com.sxy.umlmyself.dto.*;
import com.sxy.umlmyself.entity.*;
import com.sxy.umlmyself.enums.ThesisStatus;
import com.sxy.umlmyself.repository.*;
import com.sxy.umlmyself.service.AdminService;
import com.sxy.umlmyself.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CollegeRepository collegeRepository;
    private final MajorRepository majorRepository;
    private final ThesisProcessRepository thesisProcessRepository;
    private final StudentRepository studentRepository;
    private final MaterialHistoryRepository materialHistoryRepository;
    private final FinalGradeRepository finalGradeRepository;
    private final FileService fileService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserListResponseDTO getUserList(UserListRequestDTO request) {
        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "userId")
        );

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                predicates.add(cb.like(root.get("username"), "%" + request.getUsername() + "%"));
            }

            if (request.getRealName() != null && !request.getRealName().trim().isEmpty()) {
                predicates.add(cb.like(root.get("realName"), "%" + request.getRealName() + "%"));
            }

            if (request.getCollegeId() != null) {
                predicates.add(cb.equal(root.get("collegeId"), request.getCollegeId()));
            }

            if (request.getMajorId() != null) {
                predicates.add(cb.equal(root.get("majorId"), request.getMajorId()));
            }

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            } else {
                predicates.add(cb.equal(root.get("status"), 1));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserDetailDTO> userDTOs = userPage.getContent().stream()
                .map(this::convertToUserDetailDTO)
                .collect(Collectors.toList());

        if (request.getRoleId() != null) {
            userDTOs = userDTOs.stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> role.getRoleId().equals(request.getRoleId())))
                    .collect(Collectors.toList());
        }

        UserListResponseDTO response = new UserListResponseDTO();
        response.setUsers(userDTOs);
        response.setTotal(userPage.getTotalElements());
        response.setPage(request.getPage());
        response.setSize(request.getSize());
        response.setTotalPages(userPage.getTotalPages());

        return response;
    }

    @Override
    public UserDetailDTO getUserDetail(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return convertToUserDetailDTO(user);
    }

    @Override
    @Transactional
    public UserDetailDTO createUser(CreateUserRequestDTO request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (userRepository.findByPhone(request.getPhone()).isPresent()) {
                throw new BusinessException("手机号已存在");
            }
        }

        if (request.getCollegeId() != null) {
            collegeRepository.findById(request.getCollegeId())
                    .orElseThrow(() -> new BusinessException("学院不存在"));
        }

        if (request.getMajorId() != null) {
            majorRepository.findById(request.getMajorId())
                    .orElseThrow(() -> new BusinessException("专业不存在"));
        }

        if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
            throw new BusinessException("至少需要分配一个角色");
        }

        for (Integer roleId : request.getRoleIds()) {
            roleRepository.findById(roleId)
                    .orElseThrow(() -> new BusinessException("角色不存在: " + roleId));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setCollegeId(request.getCollegeId());
        user.setMajorId(request.getMajorId());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        user = userRepository.save(user);

        for (Integer roleId : request.getRoleIds()) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getUserId());
            userRole.setRoleId(roleId);
            userRoleRepository.save(userRole);
        }

        return convertToUserDetailDTO(user);
    }

    @Override
    @Transactional
    public UserDetailDTO updateUser(Integer userId, UpdateUserRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            userRepository.findByPhone(request.getPhone()).ifPresent(existingUser -> {
                if (!existingUser.getUserId().equals(userId)) {
                    throw new BusinessException("手机号已被其他用户使用");
                }
            });
        }

        if (request.getCollegeId() != null) {
            collegeRepository.findById(request.getCollegeId())
                    .orElseThrow(() -> new BusinessException("学院不存在"));
        }

        if (request.getMajorId() != null) {
            majorRepository.findById(request.getMajorId())
                    .orElseThrow(() -> new BusinessException("专业不存在"));
        }

        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getCollegeId() != null) user.setCollegeId(request.getCollegeId());
        if (request.getMajorId() != null) user.setMajorId(request.getMajorId());
        if (request.getStatus() != null) user.setStatus(request.getStatus());

        user = userRepository.save(user);

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            List<UserRole> existingRoles = userRoleRepository.findByUserId(userId);
            if (!existingRoles.isEmpty()) {
                userRoleRepository.deleteAllInBatch(existingRoles);
            }

            for (Integer roleId : request.getRoleIds()) {
                roleRepository.findById(roleId)
                        .orElseThrow(() -> new BusinessException("角色不存在: " + roleId));
            }

            List<Integer> uniqueRoleIds = request.getRoleIds().stream()
                    .distinct()
                    .collect(Collectors.toList());

            for (Integer roleId : uniqueRoleIds) {
                if (!userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    userRoleRepository.save(userRole);
                }
            }
        }

        return convertToUserDetailDTO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        user.setStatus(0);
        userRepository.save(user);
    }

    @Override
    public ArchiveStatisticsDTO getArchiveStatistics() {
        ArchiveStatisticsDTO statistics = new ArchiveStatisticsDTO();

        Long totalCompleted = thesisProcessRepository.countByStatus(ThesisStatus.completed);
        statistics.setTotalCompleted(totalCompleted);

        List<Object[]> collegeStats = thesisProcessRepository.countByStatusGroupByCollege(ThesisStatus.completed.getCode());
        List<ArchiveStatisticsDTO.CollegeStatistics> collegeStatistics = collegeStats.stream()
                .map(row -> {
                    ArchiveStatisticsDTO.CollegeStatistics stat = new ArchiveStatisticsDTO.CollegeStatistics();
                    stat.setCollegeId(row[0] != null ? ((Number) row[0]).intValue() : null);
                    stat.setCollegeName((String) row[1]);
                    stat.setCompletedCount(row[2] != null ? ((Number) row[2]).longValue() : 0L);
                    return stat;
                })
                .collect(Collectors.toList());
        statistics.setCollegeStatistics(collegeStatistics);

        List<Object[]> majorStats = thesisProcessRepository.countByStatusGroupByMajor(ThesisStatus.completed.getCode());
        List<ArchiveStatisticsDTO.MajorStatistics> majorStatistics = majorStats.stream()
                .map(row -> {
                    ArchiveStatisticsDTO.MajorStatistics stat = new ArchiveStatisticsDTO.MajorStatistics();
                    stat.setMajorId(row[0] != null ? ((Number) row[0]).intValue() : null);
                    stat.setMajorName((String) row[1]);
                    stat.setCollegeId(row[2] != null ? ((Number) row[2]).intValue() : null);
                    stat.setCollegeName((String) row[3]);
                    stat.setCompletedCount(row[4] != null ? ((Number) row[4]).longValue() : 0L);
                    return stat;
                })
                .collect(Collectors.toList());
        statistics.setMajorStatistics(majorStatistics);

        return statistics;
    }

    @Override
    public byte[] exportArchiveMaterials(List<Long> processIds) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            List<ThesisProcess> processes;
            if (processIds == null || processIds.isEmpty()) {
                processes = thesisProcessRepository.findByStatus(ThesisStatus.completed);
            } else {
                processes = processIds.stream()
                        .map(thesisProcessRepository::findById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(p -> p.getStatus() == ThesisStatus.completed)
                        .collect(Collectors.toList());
            }

            for (ThesisProcess process : processes) {
                Student student = studentRepository.findById(process.getStudentId()).orElse(null);
                if (student == null) continue;

                String folderName = String.format("%s_%s_%s_%s",
                        student.getCollege() != null ? student.getCollege().getCollegeName() : "未知学院",
                        student.getMajor() != null ? student.getMajor().getMajorName() : "未知专业",
                        student.getUser() != null ? student.getUser().getUsername() : "未知学号",
                        student.getStuName() != null ? student.getStuName() : "未知姓名");

                List<MaterialHistory> materials = materialHistoryRepository
                        .findByProcessIdOrderByUploadedAtDesc(process.getProcessId());

                for (MaterialHistory material : materials) {
                    if (!Boolean.TRUE.equals(material.getLatest())) continue;
                    try {
                        Resource resource = fileService.downloadFile(material.getFilePath());
                        String fileName = material.getOriginalFilename() != null ?
                                material.getOriginalFilename() : "未知文件";
                        String zipEntryPath = folderName + "/" + fileName;

                        ZipEntry entry = new ZipEntry(zipEntryPath);
                        zos.putNextEntry(entry);
                        resource.getInputStream().transferTo(zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        log.error("导出文件失败: {}, 错误: {}", material.getFilePath(), e.getMessage());
                    }
                }
            }

            zos.finish();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new BusinessException("导出归档材料失败: " + e.getMessage());
        }
    }

    private UserDetailDTO convertToUserDetailDTO(User user) {
        UserDetailDTO dto = new UserDetailDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setCollegeId(user.getCollegeId());
        dto.setMajorId(user.getMajorId());
        dto.setStatus(user.getStatus());

        if (user.getCollegeId() != null) {
            collegeRepository.findById(user.getCollegeId()).ifPresent(college -> {
                dto.setCollegeName(college.getCollegeName());
            });
        }

        if (user.getMajorId() != null) {
            majorRepository.findById(user.getMajorId()).ifPresent(major -> {
                dto.setMajorName(major.getMajorName());
            });
        }

        List<Role> roles = roleRepository.findRolesByUserId(user.getUserId());
        List<RoleDTO> roleDTOs = roles.stream().map(role -> {
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setRoleId(role.getRoleId());
            roleDTO.setRoleCode(role.getRoleCode());
            roleDTO.setRoleName(role.getRoleName());
            return roleDTO;
        }).collect(Collectors.toList());
        dto.setRoles(roleDTOs);

        return dto;
    }
}
