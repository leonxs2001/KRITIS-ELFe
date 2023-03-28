$(document).ready(function () {
    resetLinkHref(document.querySelector("#questionnaire-select"));
});

function resetLinkHref(select){
    let link = document.querySelector("#redirectToSituationLink");
    let href = link.getAttribute("href");
    link.setAttribute("href", href.slice(0, href.lastIndexOf("/") + 1) + select.value);
}