function onRoleChange(select){
    let role_name = select.options[select.selectedIndex].dataset.role;
    let landDiv = $("#land");
    let ressortDiv = $("#ressort");
    console.log(role_name)
    switch (role_name){
        case "ROLE_LAND":
            landDiv.show();
            ressortDiv.hide();
            break;
        case "ROLE_RESSORT":
            landDiv.hide();
            ressortDiv.show();
            break;
        default:
            landDiv.hide();
            ressortDiv.hide();
    }
}