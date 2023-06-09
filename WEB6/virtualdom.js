'use strict';
// Ecrire le reste du code ici !

render({
  state: 0,
  view: state =>
    v("div", {}, [
      v("h1", { class: "red" }, state),
      v("button", { onclick: state => state - 1 }, "subtract"),
      v("button", { onclick: state => state + 1 }, "add")
    ]),
  node: document.getElementById("my_node")
});

class Element {
  constructor(name, properties, textOrChildren) {
    this.name = name;
    this.properties = properties;
    this.textOrChildren = textOrChildren;
  }

  toDom(){

  }

}

function v(name, properties, textOrChildren) {
  return new Element(name, properties, textOrChildren);
}