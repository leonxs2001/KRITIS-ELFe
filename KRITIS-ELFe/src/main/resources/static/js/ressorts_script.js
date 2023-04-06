const branchDivHTML = '<div class="branch-div" data-sector_name="{sectorName}" data-sector_id="{sectorId}">' +
                        '<input class="hidden-branch-id-input" hidden id="ressorts0.branches0" name="ressorts[0].branches[0]" value="{branchId}">' +
                        '<button type="button" class="delete-branch-button btn btn-outline-danger bi bi-trash-fill" onclick="deleteBranchDivFromButton(this)"></button>' +
                        '<span class="branch-name-span">{branchName}</span>' +
                        '</div>';

$(document).ready(function (){
    let links = document.querySelectorAll("a");
    links.forEach(link =>{
        link.onclick = function (element){
            return confirm("Wollen Sie wirklich die Seite wechseln? Falls Sie etwas verändert und noch nicht gespeichert haben, wird es wieder gelöscht.");
        }
    });
});

function deleteBranchDivFromButton(button){
    let ressortDiv = button.closest(".ressort-div");
    let ressortName = ressortDiv.querySelector(".ressort-name");
    let branchDiv = button.closest(".branch-div");
    let branchNameSpan = branchDiv.querySelector(".branch-name-span");
    if(confirm(`Sind Sie sicher, dass Sie die Branche "${branchNameSpan.innerText}" aus dem Ressort "${ressortName.value}" löschen wollen?`)){

        let newBranchSelect = branchDiv.parentElement.querySelector(".new-branch-select");
        let hiddenBranchIdInput = branchDiv.querySelector(".hidden-branch-id-input");

        let branchId = hiddenBranchIdInput.value;
        let branchName = branchNameSpan.innerText;

        let sectorId = branchDiv.dataset.sector_id;
        let sectorName = branchDiv.dataset.sector_name;

        let sectorOptGroup = newBranchSelect.querySelector(".sector-optgroup-"+sectorId);
        if(sectorOptGroup == null){
            sectorOptGroup = document.createElement("optgroup");
            sectorOptGroup.label = sectorName;
            sectorOptGroup.classList.add("sector-optgroup-"+sectorId);
            newBranchSelect.appendChild(sectorOptGroup);
        }

        let newOption = document.createElement("option");
        newOption.value = branchId;
        newOption.innerText = branchName;
        newOption.setAttribute("data-sector_id", sectorId);
        newOption.setAttribute("data-sector_name", sectorName);
        sectorOptGroup.appendChild(newOption);

        branchDiv.remove();

        let selectBranchDiv = newBranchSelect.closest(".branch-div");
        selectBranchDiv.hidden = false;
    }
}

function addBranchFromOption(button){
    let branchDiv = button.closest(".branch-div");
    let branchElementsDiv =  branchDiv.closest(".branchelements-div");
    let newBranchSelect = branchDiv.parentElement.querySelector(".new-branch-select");
    let branchId = newBranchSelect.value;
    let branchOption = newBranchSelect.options[newBranchSelect.selectedIndex];

    //delet option (and optGroup, if empty)
    let sectorOptGroup = branchOption.parentElement;
    if(sectorOptGroup.children.length <= 1){
        sectorOptGroup.remove()
    }else{
        branchOption.remove();
    }

    let branchName = branchOption.text;
    let sectorName = branchOption.dataset.sector_name;
    let sectorId = branchOption.dataset.sector_id;
    let newBranchDivString = branchDivHTML.replaceAll("{sectorName}", sectorName)
        .replaceAll("{branchId}", branchId)
        .replaceAll("{branchName}", branchName)
        .replaceAll("{sectorId}", sectorId);
    //insert it into a template element
    let template = document.createElement('template');
    template.innerHTML = newBranchDivString;
    branchElementsDiv.insertBefore(template.content.firstChild, branchDiv.previousElementSibling);

    if(newBranchSelect.children.length == 0){
        branchDiv.hidden = true;
    }
}

function deleteRessortDivFromButton(button){
    let ressortDiv = button.closest(".ressort-div");
    let ressortName = ressortDiv.querySelector(".ressort-name");
    if(confirm(`Sind Sie sicher, dass Sie das Ressort "${ressortName.value}" löschen wollen?`)){
        ressortDiv.remove();
    }
}

function addRessort(button){
    let hiddenRessortDiv = document.querySelector(".ressort-div:nth-last-child(1)");
    let newRessortDiv = hiddenRessortDiv.cloneNode(true);
    newRessortDiv.hidden = false;

    button.parentElement.insertBefore(newRessortDiv, button);
}

function resetIndicesFromAllEntries(){
    resetAllIndicesFromRessortHeads();

    let branchElementsDivs = document.querySelectorAll(".branchelements-div");
    branchElementsDivs.forEach(branchElementsDiv => resetIndicesFromBranchElements(branchElementsDiv));
}

function resetIndicesFromBranchElements(branchElement){
    let branchIdInputs = branchElement.querySelectorAll(".hidden-branch-id-input");
    for(let i = 0; i < branchIdInputs.length; i++){
        branchIdInputs[i].id = branchIdInputs[i].id.split(".")[0] + ".branches" + i;
        branchIdInputs[i].name = branchIdInputs[i].name.split(".")[0] + ".branches[" + i + "]";
    }
}

function resetAllIndicesFromRessortHeads(){
    let ressortDivs = document.querySelectorAll(".ressort-div");
    for(let i = 0; i < ressortDivs.length - 1; i++){
        let ressortHeadDiv = ressortDivs[i].querySelector(".ressort-head-div");
        let ressortName = ressortHeadDiv.querySelector(".ressort-name");
        let ressortShortcut =  ressortHeadDiv.querySelector(".ressort-shortcut");
        let branchIds = ressortDivs[i].querySelectorAll(".hidden-branch-id-input");

        for(let j = 0; j < branchIds.length; j++){
            setRessortIndexOnNameAndId(i, branchIds[j]);
        }

        setRessortIndexOnNameAndId(i, ressortName);
        setRessortIndexOnNameAndId(i, ressortShortcut);

    }

}

function setRessortIndexOnNameAndId(index, element){
    element.name = "ressorts[" + index + "]." + element.name.split(".")[1];
    element.id = "ressorts" + index + "." + element.id.split(".")[1];
}