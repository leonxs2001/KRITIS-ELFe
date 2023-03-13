$(document).ready(function () {

    //create hover-effect for the threatmatrix nav element
    $('#dropdown-div-situation').hover(function() {
            $("#dropdown-div-situation").addClass("show");
            $("#dropdown-ul-situation").addClass("show");
            $("#dropdown-a-situation").attr("aria-expanded","true");
        },
        function() {
            $("#dropdown-div-situation").removeClass("show");
            $("#dropdown-ul-situation").removeClass("show");
            $("#dropdown-a-situation").attr("aria-expanded","false");
        });

    //create hover-effect for the admin nav element
    $('#dropdown-div-admin').hover(function() {
            $("#dropdown-div-admin").addClass("show");
            $("#dropdown-ul-admin").addClass("show");
            $("#dropdown-a-admin").attr("aria-expanded","true");
        },
        function() {
            $("#dropdown-div-admin").removeClass("show");
            $("#dropdown-ul-admin").removeClass("show");
            $("#dropdown-a-admin").attr("aria-expanded","false");
        });

});