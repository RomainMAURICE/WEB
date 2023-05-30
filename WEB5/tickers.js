"use strict";

window.onload = () => {
  let refreshButton = document.getElementById("refreshButton");
  let stockTable = document.getElementById("stockTable");
  let markText = document.getElementById("markText");
  
  // TODO

  function printAllTickers(array){

    for(let i = 0; i<array.length; i++){
      let row = document.createElement("tr");
    }
  }

  fetch("http://localhost:8080/api/ticker")
  .then(response => {
    if (response.ok) {
      return response.json();
    }
    throw Error(response.statusText);
  })
  .then(object => refreshButton.onclick = () =>{
    console.log(object)
  })
  .catch(err => console.log(err.message));

};
