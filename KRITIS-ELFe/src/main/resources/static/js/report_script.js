$(document).ready(function () {
    onSectorSelectChange(document.querySelector("#sector-select"));
});
function onReportSelectChange(select){
    let origin = $(location).attr("origin");
    let pathname = $(location).attr("pathname");

    let newUrl = origin + pathname + "?id=" + select.value;

    //set the new url
    $(location).attr("href", newUrl);
}

function onSectorSelectChange(select){
    let sectorLink = select.parentElement.querySelector("#sector-link");
    let href = sectorLink.href;

    sectorLink.href = href.replace(/sectorId=\d+/, "sectorId="+select.value);
}