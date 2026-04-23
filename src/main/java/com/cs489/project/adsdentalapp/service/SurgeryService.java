package com.cs489.project.adsdentalapp.service;

import com.cs489.project.adsdentalapp.dto.surgery.SurgeryRequest;
import com.cs489.project.adsdentalapp.dto.surgery.SurgeryResponse;

import java.util.List;

public interface SurgeryService {

    SurgeryResponse createSurgery(SurgeryRequest request);

    SurgeryResponse updateSurgery(Long surgeryId, SurgeryRequest request);

    SurgeryResponse getSurgeryById(Long surgeryId);

    List<SurgeryResponse> getAllSurgeries();

    List<SurgeryResponse> searchSurgeries(String searchString);

    void deleteSurgery(Long surgeryId);
}
