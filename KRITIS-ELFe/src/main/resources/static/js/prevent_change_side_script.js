$(document).ready(function (){
    let links = document.querySelectorAll("a");
    links.forEach(link =>{
        link.onclick = function (element){
            return confirm("Wollen Sie wirklich die Seite wechseln? Falls Sie etwas verändert und noch nicht gespeichert haben, wird es wieder gelöscht.");
        }
    });
});