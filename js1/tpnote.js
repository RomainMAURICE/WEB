"use strict";

window.onload = async () => {
  const dataTable = document.getElementById("dataTable");
  const array = [1, 2, 3,4 ];
  let size = array.length;
  // TODO

  function a(){
    for(let i = 0; i<size; i++){
      let cell = document.createElement("td");
      let input = document.createElement("input")

      input.type = "text";

      dataTable.appendChild(cell);
      cell.appendChild(input)
      
      input.value = array[i]
      input.onkeyup = () => {
        console.log(parseInt(input.value))
        if(isNaN(parseInt(input.value))){
          input.classList.add("error");
        } else {
          input.classList.remove("error");
        }
      }
    }
    return;
  }

  function retrieveOps(){
    fetch("http://localhost:8080/ops")
        .then((res) => {
          console.log(res)
        })
        .catch((err) => console.log(err.message))
  }

  retrieveOps()
  a();
};


