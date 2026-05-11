package com.sxy.umlmyself.service;

import com.sxy.umlmyself.dto.*;

public interface AdminService {

    UserListResponseDTO getUserList(UserListRequestDTO request);

    UserDetailDTO getUserDetail(Integer userId);

    UserDetailDTO createUser(CreateUserRequestDTO request);

    UserDetailDTO updateUser(Integer userId, UpdateUserRequestDTO request);

    void deleteUser(Integer userId);

    ArchiveStatisticsDTO getArchiveStatistics();

    byte[] exportArchiveMaterials(java.util.List<Long> processIds);
}
