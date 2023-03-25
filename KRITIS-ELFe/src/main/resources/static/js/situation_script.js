const colorArray = ["white", "rgb(102, 255, 102)", "rgb(255, 255, 102)", "rgb(255, 178, 102)", "rgb(255, 102, 102)"];

$(document).ready(function (){
    //go through all select-elements and set right color
    let selectList = $(".value-select");
    for(let i = 0; i < selectList.length; i++){
        noticeSelectValueChange(selectList[i]);
    }

    let branchDivList = $(".branch-div");
    for(let i = 0; i < branchDivList.length; i++){
        setBranchColorByScenarios(branchDivList[i]);
    }

    let sectorH2List = $(".sector-h2");
    for(let i = 0; i < sectorH2List.length; i++){
        setSectorColorByScenarios(sectorH2List[i]);
    }
});

function onSelectionChange(select){
    noticeSelectValueChange(select);
    let branchDiv = select.closest(".branch-div")
    setBranchColorByScenarios(branchDiv);
    setSectorColorByScenarios(document.querySelector("#" + branchDiv.dataset.sector_h2_id))
}

function noticeSelectValueChange(select){
    select.style.backgroundColor = colorArray[parseInt(select.value)];
    let textArea = select.parentElement.querySelector(".comment-textarea");
    //Todo usefull?
    if(select.value <= "1"){
        textArea.style.display = "none";
    }else{
        textArea.style.display = "block";
    }

}

function  getMaxValueFromBranchDiv(branchDiv){
    let valueSelects = branchDiv.querySelectorAll(".value-select");
    let result = "0";
    // find max
    for(let i=0; i < valueSelects.length; i++){
        if(valueSelects[i].value > result){
            result = valueSelects[i].value;
        }
    }

    return result;
}

function setBranchColorByScenarios(branchDiv){
    let icon = branchDiv.querySelector(".bi");
    setIconByGivenValue(icon, getMaxValueFromBranchDiv(branchDiv));
}

function setSectorColorByScenarios(heading){
    let branchDivs = document.querySelectorAll("." + heading.dataset.branch_class);

    let result = "0";
    // find max
    for(let i=0; i < branchDivs.length; i++){
        let branchValue = getMaxValueFromBranchDiv(branchDivs[i]);
        if(branchValue > result){
            result = branchValue;
        }
    }

    let icon = heading.querySelector(".bi");
    setIconByGivenValue(icon, result);
}

function setIconByGivenValue(icon, value){
    if(value == "0"){
        icon.classList.remove("bi-diamond-fill");
        icon.classList.add("bi-diamond");
        icon.style.color = "black";
    }else{
        icon.classList.remove("bi-diamond");
        icon.classList.add("bi-diamond-fill");
        icon.style.color = colorArray[parseInt(value)];
    }

}

function submitForm(){
    let selectList = $(".value-select");
    let firstSelectIsUnvalid = false;
    for(let i = 0; i < selectList.length; i++){
        let parentElement = selectList[i].parentElement.parentElement.querySelector(".description-p");
        if(selectList[i].value == "0"){
            parentElement.style.color = "red";
            if(!firstSelectIsUnvalid){
                firstSelectIsUnvalid = true;
                selectList[i].scrollIntoView();
            }
        }else{
            parentElement.style.color = "black";
        }
    }
    if(firstSelectIsUnvalid){
        alert("Es muss alles ausgefüllt werden. Bitte füllen sie die fehlenden Branchen komplett aus. Alle fehlenden Werte sind rot markiert.");
        return false;
    }

    $("#situation-form").submit()
    return true;
}