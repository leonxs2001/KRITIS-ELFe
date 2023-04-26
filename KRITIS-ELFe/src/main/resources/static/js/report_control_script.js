$(document).ready(function () {
    resetLinkHref(document.querySelector("#questionnaire-select"));
});

function resetLinkHref(select){
    let link = document.querySelector("#redirectToSituationLink");
    let href = link.getAttribute("href");
    link.setAttribute("href", href.slice(0, href.lastIndexOf("/") + 1) + select.value);
}

function onFormSubmit(button){
    let modalContentDiv = button.closest(".modal-content");
    let modalHeaderDiv = modalContentDiv.querySelector(".modal-header");
    let form = modalContentDiv.querySelector("form");
    let loaderDiv = modalContentDiv.querySelector(".loader");

    modalHeaderDiv.hidden = true;
    form.hidden = true;
    loaderDiv.hidden = false;

}