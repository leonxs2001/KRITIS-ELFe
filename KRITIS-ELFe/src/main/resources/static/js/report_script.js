function onReportSelectChange(select){
    let origin = $(location).attr("origin");
    let pathname = $(location).attr("pathname");

    let newUrl = origin + pathname + "?id=" + select.value;

    //set the new url
    $(location).attr("href", newUrl);
}