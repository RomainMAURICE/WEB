"use strict";
 
  window.onload = () => {
    let refreshButton = document.getElementById("refreshButton");
    let stockTable = document.getElementById("stockTable");
    let markText = document.getElementById("markText");









//---V1---

  /*function printAllTickers(array){
    for(let i = 0; i<array.length; i++){
      let row = document.createElement("tr");
      row.innerHTML=array[i];
      stockTable.appendChild(row);
    }
  }

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
        fetch("http://localhost:8080/api/stock", { method: "post" }) //une requête POST a été choisie plutot qu'un requete GET, pour des raisons de confidentialité des données, les données transmises avec GET sont visibles.
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
  }*/

//-----

    markText.onkeyup = () => {
      let markValue = parseInt(markText.value);
      let rows = stockTable.getElementsByTagName("tr");
    
      for (let i = 0; i < rows.length; i++) {
        let cells = rows[i].getElementsByTagName("td");
        
        for (let j = 1; j < cells.length; j++) {
          let stockValue = parseInt(cells[j].innerHTML);
          
          if (stockValue < markValue) {
            cells[j].style.color = "green";
          }else{
            cells[j].style.color = "black";

          }
        }
      }
    };
    

    function createTable(tickers) {
        for (let i = 0; i < tickers.length; i++) {
          let ticker = tickers[i];
      
          let row = document.createElement("tr");
      
          let tickerCell = document.createElement("td");
          tickerCell.innerHTML = ticker;
          row.appendChild(tickerCell);
      
          for (let j = 0; j < 10; j++) {
            let cell = document.createElement("td");
            cell.innerHTML = "-";
            row.appendChild(cell);
          }
      
          stockTable.appendChild(row);
        }
      }

    let compteur = 1;
      
    function fillTable(tickers, stocks, row) {
      //console.log("Tickers:", tickers);
      //console.log("Stocks:", stocks);

        for (let i = 0; i < tickers.length; i++) {
          let ticker = tickers[i];
          let stock = stocks[ticker];

          let cell = stockTable.children[i].children[row];
          //console.log(cell);
          cell.innerHTML = stock;
        }
    }

    function GetTickerAndStock () {
      let tickerPromise = fetch("http://localhost:8080/api/ticker")
        .then((ticker) => {
          if (ticker.ok) {
            return ticker.json();
          }
          throw Error(ticker.statusText);
        })
        .catch((err) => console.log("ticker error : " +  err.message))
  
      let stockPromise = fetch("http://localhost:8080/api/stock", { method: "post" })
        .then((stocks) => {
          if (stocks.ok) {
            return stocks.json();
          }
          throw Error(stocks.statusText);
        })
        .catch((err) => console.log("stock error : " + err.message))
  
      return Promise.all([tickerPromise, stockPromise])
    };
    
    refreshButton.onclick = () => {

      GetTickerAndStock()
      .then(([tickerData, stockData]) => {
        createTable(tickerData);
      });


      GetTickerAndStock()
      .then(([tickerData, stockData]) => {
        fillTable(tickerData, stockData, 1);
      });
      GetTickerAndStock()
      .then(([tickerData, stockData]) => {
        fillTable(tickerData, stockData, 2);
      });
      GetTickerAndStock()
      .then(([tickerData, stockData]) => {
        fillTable(tickerData, stockData, 3);
      });
      GetTickerAndStock()
      .then(([tickerData, stockData]) => {
        fillTable(tickerData, stockData, 4);
      });
      GetTickerAndStock()
      .then(([tickerData, stockData]) => {
        fillTable(tickerData, stockData, 5);      //désolé, j'ai rien trouvé d'autre...
      });
      GetTickerAndStock()
      .then(([tickerData, stockData]) => {
        fillTable(tickerData, stockData, 6);
      });
      GetTickerAndStock()
      .then(([tickerData, stockData]) => {
        fillTable(tickerData, stockData, 7);
      });
      GetTickerAndStock()
      .then(([tickerData, stockData]) => {
        fillTable(tickerData, stockData, 8);
      });
      GetTickerAndStock()
      .then(([tickerData, stockData]) => {
        fillTable(tickerData, stockData, 9);
      });
      GetTickerAndStock()
      .then(([tickerData, stockData]) => {
        fillTable(tickerData, stockData, 10);
      });
    };

  };
  
