package de.thb.webbaki.configuration;

import de.thb.webbaki.entity.*;
import de.thb.webbaki.repository.RoleRepository;
import de.thb.webbaki.repository.UserRepository;
import de.thb.webbaki.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;

@Component
public class SetupDataLoader implements
        ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    @Autowired
    private UserService userService;

    @Autowired
    private FederalStateService federalStateService;

    @Autowired
    private RessortService ressortService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BranchService branchService;

    @Autowired
    private SectorService sectorService;

    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {

        if (alreadySetup) {
            return;
        }

        Role bbkAdmin = createRoleIfNotFound("ROLE_BBK_ADMIN", "BBK Admin");
        Role bbkViewer = createRoleIfNotFound("ROLE_BBK_VIEWER", "BBK Viewer");
        Role land = createRoleIfNotFound("ROLE_LAND", "Länder Repräsentant");
        Role ressort = createRoleIfNotFound("ROLE_RESSORT", "Ressort Repräsentant");

        createUserIfNotFound("viewer", "leonschoenberg@gmx.de", "viewer1234", bbkViewer);
        createUserIfNotFound("admin", "leonschoenberg@gmx.de", "admin1234", bbkAdmin);
        createUserIfNotFound("land", "leonschoenberg@gmx.de", "land1234", land);
        createUserIfNotFound("ressort", "leonschoenberg@gmx.de", "ressort1234", ressort);

        createFederalStateIfNotFound("Brandenburg", "BB");
        createFederalStateIfNotFound("Baden-Württemberg", "BW");
        createFederalStateIfNotFound("Bayern", "BY");
        createFederalStateIfNotFound("Berlin", "BE");
        createFederalStateIfNotFound("Bremen", "HB");
        createFederalStateIfNotFound("Hamburg", "HH");
        createFederalStateIfNotFound("Hessen", "HH");
        createFederalStateIfNotFound("Mecklenburg-Vorpommern", "MV");
        createFederalStateIfNotFound("Niedersachsen", "NI");
        createFederalStateIfNotFound("Nordrhein-Westfalen", "NW");
        createFederalStateIfNotFound("Rheinland-Pfalz", "RP");
        createFederalStateIfNotFound("Saarland", "SL");
        createFederalStateIfNotFound("Sachsen", "SN");
        createFederalStateIfNotFound("Sachsen-Anhalt", "ST");
        createFederalStateIfNotFound("Schleswig-Holstein", "SH");
        createFederalStateIfNotFound("Thüringen", "TH");

        createRessortIfNotFound("Test1", "T1");
        createRessortIfNotFound("Test2", "T2");
        createRessortIfNotFound("Test3", "T3");


        createAllSectorsAndBranches();

        alreadySetup = true;

    }

    @Transactional
    Role createRoleIfNotFound(String name, String representation) {

        Role role = roleService.getRoleByName(name);
        if (role == null) {
            role = new Role(name, representation);
            role = roleService.createRole(role);
        }
        return role;
    }

    @Transactional
    User createUserIfNotFound(final String username, String email, String password, Role role) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            user = new User();

            user.setRoles(Collections.singletonList(role));
            user.setPassword(passwordEncoder.encode(password));
            user.setEnabled(true);
            user.setUsername(username);
            user.setEmail(email);
        }

        user = userService.createUser(user);

        return user;
    }

    @Transactional
    FederalState createFederalStateIfNotFound(String name, String shortcut){
        FederalState federalState = federalStateService.getFederalStateByName(name);
        if(federalState == null){
            federalState = federalStateService.createFederalState(new FederalState(name, shortcut));
        }
        return federalState;
    }

    @Transactional
    Ressort createRessortIfNotFound(String name, String shortcut){
        Ressort ressort = ressortService.getRessortByName(name);
        if(ressort == null){
            ressort = ressortService.createRessort(new Ressort(name, shortcut));
        }
        return ressort;
    }

    @Transactional
    void createAllSectorsAndBranches(){
        Sector sector = createSectorIfNotFound("Energie");
        createBranchIfNotFound("Elektrizität", sector);
        createBranchIfNotFound("Gas", sector);
        createBranchIfNotFound("Mineralöl", sector);
        createBranchIfNotFound("Fernwärme", sector);

        sector = createSectorIfNotFound("Gesundheit");
        createBranchIfNotFound("medizinische Versorgung", sector);
        createBranchIfNotFound("Arzneimittel und Impfstoffe", sector);
        createBranchIfNotFound("Labore", sector);

        sector = createSectorIfNotFound("Informationstechnik und Telekommunikation");
        createBranchIfNotFound("Telekommunikationstechnik", sector);
        createBranchIfNotFound("Informationstechnik", sector);

        sector = createSectorIfNotFound("Transport und Verkehr");
        createBranchIfNotFound("Luftfahrt", sector);
        createBranchIfNotFound("Seeschifffahrt", sector);
        createBranchIfNotFound("Binnenschifffahrt", sector);
        createBranchIfNotFound("Schienenverkehr", sector);
        createBranchIfNotFound("Straßenvekehr", sector);
        createBranchIfNotFound("Logistik", sector);
        createBranchIfNotFound("ÖPNV", sector);

        sector = createSectorIfNotFound("Medien und Kultur");
        createBranchIfNotFound("Rundfunk (Fernsehen und Radio)", sector);
        createBranchIfNotFound("gedruckte und elektronische Presse", sector);
        createBranchIfNotFound("Kulturgut", sector);
        createBranchIfNotFound("symbolträchtige Bauwerke", sector);

        sector = createSectorIfNotFound("Wasser");
        createBranchIfNotFound("öffentliche Wasserversorgung", sector);
        createBranchIfNotFound("öffentliche Abwasserbeseitigung", sector);

        sector = createSectorIfNotFound("Finanz- und Versicherungswesen");
        createBranchIfNotFound("Banken", sector);
        createBranchIfNotFound("Börsen", sector);
        createBranchIfNotFound("Versicherungen", sector);
        createBranchIfNotFound("Finanzdienstleister", sector);

        sector = createSectorIfNotFound("Ernährung");
        createBranchIfNotFound("Ernährungswissenschaft", sector);
        createBranchIfNotFound("Lebensmittelhandel", sector);

        sector = createSectorIfNotFound("Staat und Verwaltung");
        createBranchIfNotFound("Regierung und Verwaltung", sector);
        createBranchIfNotFound("Parlament", sector);
        createBranchIfNotFound("Justizeinrichtungen", sector);
        createBranchIfNotFound("Notfall/Rettungswesen", sector);

    }

    @Transactional
    Sector createSectorIfNotFound(String name){
        Sector sector = sectorService.getSectorByName(name);
        if(sector == null){
            sector = sectorService.createSector(new Sector(name));
        }

        return sector;
    }

    @Transactional
    Branch createBranchIfNotFound(String name, Sector sector){
        Branch branch = branchService.getBranchByName(name);
        if(sector == null){
            branch = branchService.createBranch(new Branch(name, sector));
        }

        return branch;
    }
}