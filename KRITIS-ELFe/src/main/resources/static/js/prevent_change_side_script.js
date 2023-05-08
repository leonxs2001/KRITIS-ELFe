$(document).ready(function (){
    let data_changed_element = document.createElement("data");
    data_changed_element.value = "false";

    let links = document.querySelectorAll("a.nav-link");
    links.forEach(link =>{
        link.onclick = function (event){
            if(data_changed_element.value == "true") {
                return confirm("Wollen Sie wirklich die Seite wechseln? Falls Sie etwas verändert und noch nicht gespeichert haben, wird es wieder gelöscht.");
            }
        }
    });

    let inputs = document.querySelectorAll("input, textarea, select");
    inputs.forEach(element =>{
        if(!element.hidden){
            element.addEventListener("change", function (event){
                data_changed_element.value = "true";
            });
        }
    });

    let btns = document.querySelectorAll(".btn");

    btns.forEach(btn =>{
        btn.addEventListener("click", function (event){
            data_changed_element.value = "true";
        });
    });
});