package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.entity.Branch;
import de.thb.kritis_elfe.repository.BranchRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BranchService {
    private final BranchRepository branchRepository;

    public List<Branch> getAllBranches(){return branchRepository.findAll();}

    public Branch getBranchByName(String name){
        return branchRepository.findByName(name);
    }

    public Branch createBranch(Branch branch){return branchRepository.save(branch);}

}
