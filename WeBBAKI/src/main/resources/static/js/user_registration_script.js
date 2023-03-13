function onRoleChange(select){
    let select_value = select.value;
    let landDiv = $("#land");
    let ressortDiv = $("#ressort");
    console.log(select_value);

    switch (select_value){
        case "land":
            console.log("ja")
            landDiv.show();
            ressortDiv.hide();
            break;
        case "ressort":
            console.log("ja2")
            landDiv.hide();
            ressortDiv.show();
            break;
        default:
            console.log("ja3")
            landDiv.hide();
            ressortDiv.hide();
    }
}