package com.hotrodoan.service;

import com.hotrodoan.model.RegularPass;
import com.hotrodoan.model.dto.RegularPassSub;

public interface RegularPassSubService {
    RegularPassSub createRegularPassSub(RegularPass regularPass);
    RegularPassSub updateRegularPassSub(RegularPassSub regularPassSub);
    RegularPassSub getRegularPassSub(Long id);
    void deleteRegularPassSub(Long id);
}
