package de.thb.kritis_elfe.configuration;

import de.thb.kritis_elfe.entity.*;
import de.thb.kritis_elfe.enums.ScenarioType;
import de.thb.kritis_elfe.service.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class SetupDataLoader implements
        ApplicationListener<ContextRefreshedEvent> {

    private static boolean alreadySetup = false;

    private final UserService userService;
    private final FederalStateService federalStateService;
    private final RessortService ressortService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final BranchService branchService;
    private final SectorService sectorService;
    private final ScenarioService scenarioService;

    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {

        if (alreadySetup) {
            return;
        }

        FederalState brandenburg = createFederalStates();
        Ressort ressort3 = createRessorts(createAllSectorsAndBranches());

        Role bbkAdmin = createRoleIfNotFound("ROLE_BBK_ADMIN", "BBK Admin");
        Role bbkViewer = createRoleIfNotFound("ROLE_BBK_VIEWER", "BBK Viewer");
        Role land = createRoleIfNotFound("ROLE_LAND", "Länder Repräsentant");
        Role ressort = createRoleIfNotFound("ROLE_RESSORT", "Ressort Repräsentant");
        Role office = createRoleIfNotFound("ROLE_GESCHÄFTSSTELLE", "Geschäftsstelle");

        createUserIfNotFound("viewer", "leonschoenberg@gmx.de", "viewer1234", bbkViewer, null, null);
        createUserIfNotFound("admin", "leonschoenberg@gmx.de", "admin1234", bbkAdmin, null, null);
        createUserIfNotFound("ressort", "leonschoenberg@gmx.de", "ressort1234", ressort, null, ressort3);
        createUserIfNotFound("office", "leonschoenberg@gmx.de", "office1234", office, null, null);
        createUserIfNotFound("land", "leonschoenberg@gmx.de", "land1234", land, brandenburg, null);

        createScenarios();

        alreadySetup = true;

    }

    @Transactional
    void createScenarios(){
        createScenarioIfNotFound("1. Inwieweit ist die Bereitstellung der Dienstleistungen in der benannten Branche aktuell eingeschränkt? \n" +
                "(Gibt es Einschränkungen und wie sind sie zu bewerten?)\n", ScenarioType.AUSWAHL, (short)1);
        createScenarioIfNotFound("5. Inwieweit erwarten Sie mittel- und langfristig Einschränkungen bei der Bereitstellung der Dienstleistungen in der benannten Branche? ", ScenarioType.AUSWAHL, (short)3);
        createScenarioIfNotFound("4. Besteht weiterer Bedarf an staatlicher Unterstützung, um den o. g. aktuellen / kurzfristig zu erwartenden Einschränkungen vorzubeugen oder ihnen entgegenzuwirken?", ScenarioType.TEXT, (short)2);

    }

    @Transactional
    Ressort createRessorts(List<Sector> sectors) {
        List<Branch> branchList = new ArrayList<>();
        branchList.addAll(sectors.get(0).getBranches());
        branchList.addAll(sectors.get(1).getBranches());
        createRessortIfNotFound("Test1", "T1", branchList);

        branchList = new ArrayList<>();
        branchList.addAll(sectors.get(2).getBranches());
        branchList.addAll(sectors.get(3).getBranches());
        createRessortIfNotFound("Test2", "T2", branchList);

        branchList = new ArrayList<>();
        branchList.addAll(sectors.get(4).getBranches());
        branchList.addAll(sectors.get(5).getBranches());
        createRessortIfNotFound("Test4", "T4", branchList);
        branchList = new ArrayList<>();
        branchList.addAll(sectors.get(6).getBranches());
        branchList.addAll(sectors.get(7).getBranches());
        createRessortIfNotFound("Test5", "T5", branchList);

        branchList = new ArrayList<>();
        branchList.addAll(sectors.get(8).getBranches());
        branchList.addAll(sectors.get(9).getBranches());
        return createRessortIfNotFound("Test6", "T6", branchList);
    }

    @Transactional
    FederalState createFederalStates() {
        FederalState brandenburg = createFederalStateIfNotFound("Brandenburg", "BB");
        createFederalStateIfNotFound("Baden-Württemberg", "BW");
        createFederalStateIfNotFound("Bayern", "BY");
        createFederalStateIfNotFound("Berlin", "BE");
        createFederalStateIfNotFound("Bremen", "HB");
        createFederalStateIfNotFound("Hamburg", "HH");
        createFederalStateIfNotFound("Hessen", "HE");
        createFederalStateIfNotFound("Mecklenburg-Vorpommern", "MV");
        createFederalStateIfNotFound("Niedersachsen", "NI");
        createFederalStateIfNotFound("Nordrhein-Westfalen", "NW");
        createFederalStateIfNotFound("Rheinland-Pfalz", "RP");
        createFederalStateIfNotFound("Saarland", "SL");
        createFederalStateIfNotFound("Sachsen", "SN");
        createFederalStateIfNotFound("Sachsen-Anhalt", "ST");
        createFederalStateIfNotFound("Schleswig-Holstein", "SH");
        createFederalStateIfNotFound("Thüringen", "TH");
        return brandenburg;
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
    User createUserIfNotFound(final String username, String email, String password, Role role, FederalState federalState, Ressort ressort) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            user = new User();

            user.setRoles(Collections.singletonList(role));
            user.setPassword(passwordEncoder.encode(password));
            user.setEnabled(true);
            user.setUsername(username);
            user.setEmail(email);
            user.setFederalState(federalState);
            user.setRessort(ressort);
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
    Ressort createRessortIfNotFound(String name, String shortcut, List<Branch> branches){
        Ressort ressort = ressortService.getRessortByName(name);
        if(ressort == null){
            ressort = ressortService.createRessort(new Ressort(name, shortcut, branches));
        }
        return ressort;
    }

    @Transactional
    Scenario createScenarioIfNotFound(String description, ScenarioType scenarioType, short positionInRow){
        Scenario scenario = scenarioService.getScenarioByDescriptionAndActive(description);
        if(scenario == null){
            scenario = scenarioService.createScenario(new Scenario(description, scenarioType, positionInRow));
        }
        return scenario;
    }

    @Transactional
    List<Sector> createAllSectorsAndBranches(){
        List<Sector> sectors = new ArrayList<>();
        Sector sector = createSectorIfNotFound("Energie");
        sector.getBranches().add(createBranchIfNotFound("Elektrizität", sector));
        sector.getBranches().add(createBranchIfNotFound("Gas", sector));
        sector.getBranches().add(createBranchIfNotFound("Mineralöl", sector));
        sector.getBranches().add(createBranchIfNotFound("Fernwärme", sector));
        sectors.add(sector);

        sector = createSectorIfNotFound("Gesundheit");
        sector.getBranches().add(createBranchIfNotFound("medizinische Versorgung", sector));
        sector.getBranches().add(createBranchIfNotFound("Arzneimittel und Impfstoffe", sector));
        sector.getBranches().add(createBranchIfNotFound("Labore", sector));
        sectors.add(sector);

        sector = createSectorIfNotFound("Informationstechnik und Telekommunikation");
        sector.getBranches().add(createBranchIfNotFound("Telekommunikation", sector));
        sector.getBranches().add(createBranchIfNotFound("Informationstechnik", sector));
        sectors.add(sector);

        sector = createSectorIfNotFound("Transport und Verkehr");
        sector.getBranches().add(createBranchIfNotFound("Luftfahrt", sector));
        sector.getBranches().add(createBranchIfNotFound("Seeschifffahrt", sector));
        sector.getBranches().add(createBranchIfNotFound("Binnenschifffahrt", sector));
        sector.getBranches().add(createBranchIfNotFound("Schienenverkehr", sector));
        sector.getBranches().add(createBranchIfNotFound("Straßenverkehr", sector));
        sector.getBranches().add(createBranchIfNotFound("Logistik", sector));
        sectors.add(sector);

        sector = createSectorIfNotFound("Medien und Kultur");
        sector.getBranches().add(createBranchIfNotFound("Rundfunk (Fernsehen und Radio)", sector));
        sector.getBranches().add(createBranchIfNotFound("gedruckte und elektronische Presse", sector));
        sector.getBranches().add(createBranchIfNotFound("Kulturgut", sector));
        sector.getBranches().add(createBranchIfNotFound("symbolträchtige Bauwerke", sector));
        sectors.add(sector);

        sector = createSectorIfNotFound("Wasser");
        sector.getBranches().add(createBranchIfNotFound("öffentliche Wasserversorgung", sector));
        sector.getBranches().add(createBranchIfNotFound("öffentliche Abwasserbeseitigung", sector));
        sectors.add(sector);

        sector = createSectorIfNotFound("Finanz- und Versicherungswesen");
        sector.getBranches().add(createBranchIfNotFound("Banken", sector));
        sector.getBranches().add(createBranchIfNotFound("Börsen", sector));
        sector.getBranches().add(createBranchIfNotFound("Versicherungen", sector));
        sector.getBranches().add(createBranchIfNotFound("Finanzdienstleister", sector));
        sectors.add(sector);

        sector = createSectorIfNotFound("Siedlungsabfallentsorgung");
        sector.getBranches().add(createBranchIfNotFound("Siedlungsabfallentsorgung", sector));
        sectors.add(sector);

        sector = createSectorIfNotFound("Ernährung");
        sector.getBranches().add(createBranchIfNotFound("Ernährungswirtschaft", sector));
        sector.getBranches().add(createBranchIfNotFound("Lebensmittelhandel", sector));
        sectors.add(sector);

        sector = createSectorIfNotFound("Staat und Verwaltung");
        sector.getBranches().add(createBranchIfNotFound("Regierung und Verwaltung", sector));
        sector.getBranches().add(createBranchIfNotFound("Parlament", sector));
        sector.getBranches().add(createBranchIfNotFound("Justizeinrichtungen", sector));
        sector.getBranches().add(createBranchIfNotFound("Notfall-/Rettungswesen & Katastrophenschutz", sector));
        sectors.add(sector);

        return sectors;

    }

    @Transactional
    Sector createSectorIfNotFound(String name){
        Sector sector = sectorService.getSectorByName(name);
        if(sector == null){
            sector = sectorService.createSector(new Sector(name));
            sector.setBranches(new ArrayList<>());
        }

        return sector;
    }

    @Transactional
    Branch createBranchIfNotFound(String name, Sector sector){
        Branch branch = branchService.getBranchByName(name);
        if(branch == null){
            branch = branchService.createBranch(new Branch(name, sector));
        }

        return branch;
    }
}