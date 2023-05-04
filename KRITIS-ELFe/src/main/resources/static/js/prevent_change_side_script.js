$(document).ready(function (){
    let links = document.querySelectorAll("a.nav-link");
    links.forEach(link =>{
        link.onclick = function (event){
            let classList = link.parentElement.classList;
            return confirm("Wollen Sie wirklich die Seite wechseln? Falls Sie etwas verändert und noch nicht gespeichert haben, wird es wieder gelöscht.");
        }
    });
});