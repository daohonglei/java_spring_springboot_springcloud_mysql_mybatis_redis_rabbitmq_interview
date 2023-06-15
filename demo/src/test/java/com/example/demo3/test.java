/*
package com.example.demo3;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

*/
/**
 * @Author 955
 * @Date 2022-06-21 15:01
 * @Description
 *//*

@Slf4j
public class test {
    */
/**
     * 初始化流程部署
     *//*

    @Test
    public void testDeployment() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("processes/test.bpmn20.xml").addClasspathResource("processes/test.png").name("请假申请流程").deploy();
        System.out.println("流程部署id：" + deployment.getId());
        System.out.println("流程部署名称：" + deployment.getName());
    }

    */
/**
     * 开始流程
     *//*

    @Test
    public void testStartProcess() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("test");

        System.out.println("流程部署id：" + processInstance.getId());
        System.out.println("流程部署名称：" + processInstance.getName());
        System.out.println("processInstanceId：" + processInstance.getProcessInstanceId());
    }

    */
/**
     * 查询流转到该所属角色的任务
     *//*

    @Test
    public void testFindPersonalTaskList() {
        //对应各流程节点流转下一步人名称，这里第一步从worker开始
        //调用下方completTask方法可通过审批，再查询下一个名称leader，以此类推直到结束，因为流程图没有不通过的情况所以暂不考虑
        String assignee = "worker";
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        List<Task> list1 = taskService.createTaskQuery().processDefinitionKey("test").list();
        List<Task> list = taskService.createTaskQuery().processDefinitionKey("test").taskAssignee(assignee).list();
        for (Task task : list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    */
/**
     * 完成任务
     *//*

    @Test
    public void completTask() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        //根据流程key和任务的负责人 查询任务
        //返回一个任务对象
        //对应各流程节点流转下一步人名称，这里第一步从worker开始，分别为worker-->leader-->finance
        Task task = taskService.createTaskQuery().processDefinitionKey("test").taskAssignee("worker").singleResult();
        //完成任务，参数：任务id
        taskService.complete(task.getId());
    }

    */
/**
     * 查询出当前所有的流程定义
     *//*

    @Test
    public void queryProcessDefinition() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        //查询出当前所有的流程定义
        List<ProcessDefinition> list = processDefinitionQuery.processDefinitionKey("test").orderByProcessDefinitionVersion().desc().list();
        //输出流程定义信息
        for (ProcessDefinition processDefinition : list) {
            System.out.println("流程定义 id=" + processDefinition.getId());
            System.out.println("流程定义 name=" + processDefinition.getName());
            System.out.println("流程定义 key=" + processDefinition.getKey());
            System.out.println("流程部署id =" + processDefinition.getDeploymentId());
            System.out.println("<=========================================>");
        }
    }

    */
/**
     * 删除流程
     *//*

    @Test
    public void deleteDeployment() {
        String deploymentId = "1";
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //通过流程引擎获取repositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //删除流程定义，如果该流程定义已有流程实例启动则删除报错
        repositoryService.deleteDeployment(deploymentId);
        //设置为true，则有流程实例在启动也可以强制删除
//        repositoryService.deleteDeployment(deploymentId,true);
    }

    */
/**
     * 输出流程文件和流程图到文件夹
     *
     * @throws Exception
     *//*

    @Test
    public void queryBpmnFile() throws Exception {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("test").singleResult();
        //通过流程定义信息，得到部署id
        String deploymentId = processDefinition.getDeploymentId();
        //得到png图片流
        InputStream pngInput = repositoryService.getResourceAsStream(deploymentId, processDefinition.getDiagramResourceName());
        //得到bpmn文件流
        InputStream bpmnInput = repositoryService.getResourceAsStream(deploymentId, processDefinition.getResourceName());
        File file_png = new File("C:\\Users\\HYGK\\Desktop\\bpmn\\test.png");
        File file_bpmn = new File("C:\\Users\\HYGK\\Desktop\\bpmn\\test.bpmn");
        FileOutputStream pngOut = new FileOutputStream(file_png);
        FileOutputStream bpmnOut = new FileOutputStream(file_bpmn);
        IOUtils.copy(pngInput, pngOut);
        IOUtils.copy(bpmnInput, bpmnOut);
        pngOut.close();
        bpmnOut.close();
    }

    */
/**
     * 根据instanceId查询整个流程
     *//*

    @Test
    public void findHistoryInfo() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        HistoryService historyService = processEngine.getHistoryService();
        //获取actinst表的查询对象
        HistoricActivityInstanceQuery instanceQuery = historyService.createHistoricActivityInstanceQuery();
        //根据instanceId查询整个流程
        instanceQuery.processInstanceId("5001").orderByHistoricActivityInstanceStartTime().asc();
        List<HistoricActivityInstance> list = instanceQuery.list();
        for (HistoricActivityInstance historicActivityInstance : list) {
            System.out.println(historicActivityInstance.getActivityId());
            System.out.println(historicActivityInstance.getActivityName());
            System.out.println(historicActivityInstance.getProcessDefinitionId());
            System.out.println(historicActivityInstance.getCalledProcessInstanceId());
            System.out.println("<=========================================>");
        }
    }

}


*/
