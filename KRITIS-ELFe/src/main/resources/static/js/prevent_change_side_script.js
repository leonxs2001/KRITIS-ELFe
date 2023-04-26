$(document).ready(function (){
    let links = document.querySelectorAll("a");
    links.forEach(link =>{
        link.onclick = function (element){
            let classList = link.parentElement.classList;
            if(!classList.contains("btn")) {
                return confirm("Wollen Sie wirklich die Seite wechseln? Falls Sie etwas verändert und noch nicht gespeichert haben, wird es wieder gelöscht.");
            }
        }
    });
});