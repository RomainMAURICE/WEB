"use strict";

window.onload = () => {
  let refreshButton = document.getElementById("refreshButton");
  let stockTable = document.getElementById("stockTable");
  let markText = document.getElementById("markText");
  
  
  // TODO

  /*function printAllTickers(array){
    for(let i = 0; i<array.length; i++){
      let row = document.createElement("tr");
      row.innerHTML=array[i];
      stockTable.appendChild(row);
    }
  }*/

  // function printAllStock(array){
  //   for(let i = 0; i<Object.keys(array).length; i++){
  //     for(let key in array){
  //       if(key==tickerOrder[i]){
  //         console.log(key);
  //         console.log(array[key]);
  //         let cell = document.createElement("td");
  //         cell.innerHTML=array[key];
  //         //stockTable.children[i].appendChild(cell);
  //         //console.log(stockTable.children[i]);
  //         break;
  //       }
  //     }
  //   }
  // }

  function printTickerAndStock(tickers, stocks) {
    for (let z = 0; z < tickers.length; z++) {
      let ticker = tickers[z];
      let stock = stocks[ticker];

      console.log(ticker + ": " + stock);
    }

    for (let i = 0; i < tickers.length; i++) {
        let row = document.createElement("tr");
        row.innerHTML=tickers[i];
        stockTable.appendChild(row);

        let cell = document.createElement("td");
        cell.innerHTML=stocks[tickers[i]];
        row.appendChild(cell);

        for(let j = 0; j<=10; j++){
          let cell = document.createElement("td");
          cell.innerHTML="-";
          row.appendChild(cell);
        }
    }


  }

  let times=0;

  function printTab(){
    fetch("http://localhost:8080/api/ticker")
      .then((response) => {
        if (response.ok) {
          return response.json();
        }
        throw Error(response.statusText);
      })
      .then((tickers) => {
        fetch("http://localhost:8080/api/stock", { method: "post" })
          .then((response) => {
            if (response.ok) {
              return response.json();
            }
            throw Error(response.statusText);
          })
          .then((stocks) => {
            //console.log(stocks);
            //console.log(tickers);
            printTickerAndStock(tickers, stocks);
          })
          .catch((err) => console.log(err.message));
      })
      .catch((err) => console.log(err.message));
  }

  refreshButton.onclick= () =>{
    printTab();
  }

}
