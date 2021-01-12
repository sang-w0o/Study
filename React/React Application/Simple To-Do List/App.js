
import React, {Component} from 'react';
import {TodoBanner} from './TodoBanner';
import {TodoCreator} from './TodoCreator';
import {TodoRow} from './TodoRow';
import {VisibilityControl} from './VisibilityControl';

export default class App extends Component {

  constructor(props) {
    super(props);
    this.state = {
      userName:'Sangwoo',
      todoItems: [{
        action:'Linux VI Practice', done:true},
        {action:'Linux CMD Practice', done:false},
        {action:'React Study chapter1-3', done:false},
        {action:'Go HOME', done:false}],
        showCompleted:true
    }
  }

  updateNewTextValue = (event) => {
    this.setState({newItemText : event.target.value});
  }

  // 새로운 Todo를 만들어내는 함수. TodoCreator의 callback으로 들어간다.
  createNewTodo = (task) => {
    if(!this.state.todoItems.find(item=>item.action===task)){
      this.setState({
        todoItems: [...this.state.todoItems, {
          action:task, done:false }],
        newItemText:""
      }, () => localStorage.setItem("todos", JSON.stringify(this.state)));
    }
  }

  // Todo의 action이 true, false로 사용자에 의해 바뀌면 배열 정보도 그렇게 바꾼다.
  toggleTodo = (todo) => this.setState({
    todoItems:this.state.todoItems.map(item=>item.action === todo.action ? {...item, done:!item.done} : item)
  });

  todoTableRows = (doneValue) => this.state.todoItems.filter(item=>item.done === doneValue).map(item=>
    <TodoRow key={item.action} item={item} callback={this.toggleTodo}/>)
  

  // LocalStorage 객체를 통해 다른 사이트로의 이동 후 복귀, 브라우저의 새로고침 등에서도 데이터를 유지한다.
  componentDidMount = () => {
    let data = localStorage.getItem("todos");
    this.setState(data != null ? JSON.parse(data) : {
      userName:'Sangwoo',
      todoItems: [{
        action:'Linux VI Practice', done:true},
        {action:'Linux CMD Practice', done:false},
        {action:'React Study chapter1-3', done:false},
        {action:'Go HOME', done:false}],
        showCompleted:true
    })
  }
  render = () =>
      <div>
        <TodoBanner name={this.state.userName} tasks={this.state.todoItems} />
        <div className='container-fluid'>
          <TodoCreator callback={this.createNewTodo} />
          <table className='table table-striped table-bordered'>
            <thead>
              <tr><th>Description</th><th>Done</th></tr>
            </thead>
            <tbody>{this.todoTableRows(false)}</tbody>
          </table>
          <div className='bg-secondary text-white text-center p-2'>
            <VisibilityControl description="Completed Tasks"
              isChecked={this.state.showCompleted}
              callback={(checked) => this.setState({showCompleted:checked})}/>
          </div>
          {this.state.showCompleted && 
            <table className='table table-striped table-bordered'>
                <thead>
                  <tr><th>Description</th><th>Done</th></tr>
                </thead>
                <tbody>
                  {this.todoTableRows(true)}
                </tbody>
            </table>}
        </div>
       </div>
  }
