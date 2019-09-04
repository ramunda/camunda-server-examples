import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.demo.CamundaApplication;
import org.camunda.demo.dto.InvoiceDTO;
import org.camunda.demo.dto.MovementDTO;
import org.camunda.demo.exception.exceptions.BasicException;
import org.camunda.demo.exception.exceptions.DataNotFoundException;
import org.camunda.demo.exception.exceptions.InvalidParametersException;
import org.camunda.demo.model.ParameterModel;
import org.camunda.demo.model.ProcessModel;
import org.camunda.demo.model.TaskModel;
import org.camunda.demo.repo.MilestoneRepository;
import org.camunda.demo.repo.Milestones;
import org.camunda.demo.repo.ProcessMilestones;
import org.camunda.demo.repo.ProcessRepository;
import org.camunda.demo.service.ProcessService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= CamundaApplication.class)
@Transactional
@Rollback(true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProcessServiceUnitTest {

    @MockBean
    private ProcessRepository processRepository;

    @MockBean
    private MilestoneRepository milestoneRepository;

    @Autowired
    private ProcessService processService;

    @Autowired
    private RuntimeService runtimeService;

    @Test
    public void cancelProcInstanceSuccess () throws BasicException{
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("Process_08rb4ct", (Map<String, Object>) null);

        assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult())
                .isNotNull();

        processService.cancelProcInstance(pi.getId());

        assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult())
                .isNull();
    }

    @Test
    public void cancelNonExistingProcInstance (){
        assertThatExceptionOfType(DataNotFoundException.class)
                .isThrownBy(
                        () -> processService.cancelProcInstance("123")
                );
    }

    @Test
    public void cancelAlreadyCanceledProcInstance () throws BasicException{

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("Process_08rb4ct", (Map<String, Object>) null);

        assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult())
                .isNotNull();

        processService.cancelProcInstance(pi.getId());

        assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult())
                .isNull();

        assertThatExceptionOfType(DataNotFoundException.class)
                .isThrownBy(
                        () -> processService.cancelProcInstance(pi.getId())
                );
    }

    @Test
    public void startProcessInstanceSucess () throws BasicException{

        LinkedHashMap<Object, Object> clientDetails = new LinkedHashMap<>();
        clientDetails.put("nif","263884205");
        clientDetails.put("accountId","263884205");
        clientDetails.put("personId","112341198");

        ProcessModel proc = new ProcessModel("Process_08rb4ct",
                Stream.of(
                        new ParameterModel("client","263884205"),
                        new ParameterModel("clientDetails",clientDetails),
                        new ParameterModel("user","user123")
                )
        );

        List<ParameterModel> expectedOutParams = firstTaskOutParams();

        ProcessModel pi = processService.startProcessInstance(proc);

        assertThat(pi).isNotNull();

        TaskModel activeTask = pi.getActiveTask();

        assertThat(activeTask.getId()).isNotNull();
        assertThat(activeTask.getMilestone()).isEqualTo("Movimentos");

        assertThat(activeTask.getOutParameters()).containsExactlyInAnyOrderElementsOf(expectedOutParams);
        assertThat(activeTask.getInParameters()).isEmpty();

        List<Milestones> expectedMilestones = new ArrayList<>();
        Milestones m1 = new Milestones();
        m1.setMilestoneId("3");
        m1.setMilestoneName("Movimentos");
        m1.setTasks(new String [] {"Task_1wayfyf"});
        Milestones m2 = new Milestones();
        m2.setMilestoneId("4");
        m2.setMilestoneName("Referência Multibanco");
        m2.setTasks(new String [] {"Task_19epn2w"});

        expectedMilestones.add(m1);
        expectedMilestones.add(m2);

        assertThat(pi.getMilestones()).containsExactlyInAnyOrderElementsOf(expectedMilestones);
        assertThat(pi.getProcDefKey()).isEqualTo("Process_08rb4ct");
    }

    @Test
    public void procHistorySucess () throws BasicException{
        Map<String, Object> p1vars = new HashMap<>();
        p1vars.put("user", "999");
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("Process_08rb4ct", p1vars);

        Map<String, Object> p2vars = new HashMap<>();
        p2vars.put("user", "111");

        ProcessInstance pi2 = runtimeService.startProcessInstanceByKey("Process_08rb4ct", p2vars);

        List<ProcessModel> procHistory = processService.procHistory("Process_08rb4ct", "user", "111").collect(Collectors.toList());
        assertThat(procHistory).hasSize(1);
        ProcessModel processModel = procHistory.get(0);

        List<ParameterModel> procVars = processModel.getVariables().collect(Collectors.toList());

        assertThat(procVars).hasSize(1);
        assertThat(procVars).containsExactlyInAnyOrderElementsOf(singletonList(new ParameterModel("user", "111")));
        assertThat(processModel.getState()).isEqualTo("ACTIVE");

        procHistory = processService.procHistory("Process_08rb4ct",null, null).collect(Collectors.toList());
        assertThat(procHistory).hasSize(2);

        processModel = procHistory.get(0);
        assertThat(processModel.getVariables()).containsExactlyInAnyOrderElementsOf(singletonList(new ParameterModel("user", "111")));
        assertThat(processModel.getState()).isEqualTo("ACTIVE");

        runtimeService.deleteProcessInstance(processModel.getProcInstId(), null);

        procHistory = processService.procHistory("Process_08rb4ct", null, null).collect(Collectors.toList());
        assertThat(procHistory).hasSize(2);
        processModel = procHistory.get(0);

        assertThat(processModel.getState()).isEqualTo("INTERNALLY_TERMINATED");
    }

    @Test
    public void nextTaskSucess () throws BasicException {

        List<ParameterModel> expectedOutParams = firstTaskOutParams();
        List<ParameterModel> expectedOutParams2 = secondTaskOutParams();

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("Process_08rb4ct", (Map<String, Object>) null);

        TaskModel currentTask = processService.getCurrentTask(pi.getId(),false);

        // We need this because stream can only be consumed once
        List<ParameterModel> outParams = currentTask.getOutParameters().collect(Collectors.toList());
        List<ParameterModel> inParams = currentTask.getInParameters().collect(Collectors.toList());

        assertThat(currentTask.getId()).isNotNull();
        assertThat(currentTask.getMilestone()).isEqualTo("Movimentos");
        assertThat(outParams).containsExactlyInAnyOrderElementsOf(expectedOutParams);
        assertThat(inParams).isEmpty();


        // We need this because stream can only be consumed once
        currentTask.setOutParameters(outParams.stream());
        currentTask.setInParameters(inParams.stream());

        TaskModel nextTask = processService.nextTask(pi.getId(), currentTask , true);

        outParams = nextTask.getOutParameters().collect(Collectors.toList());
        inParams = nextTask.getInParameters().collect(Collectors.toList());

        assertThat(nextTask.getId()).isNotNull();
        assertThat(nextTask.getMilestone()).isEqualTo("Referência Multibanco");
        assertThat(outParams).containsExactlyInAnyOrderElementsOf(expectedOutParams2);
        assertThat(inParams).isEmpty();

        nextTask.setOutParameters(outParams.stream());
        nextTask.setInParameters(inParams.stream());

        nextTask = processService.nextTask(pi.getId(), nextTask , false);

        outParams = nextTask.getOutParameters().collect(Collectors.toList());
        inParams = nextTask.getInParameters().collect(Collectors.toList());

        assertThat(nextTask.getId()).isNotNull();
        assertThat(nextTask.getMilestone()).isEqualTo("Movimentos");
        assertThat(outParams).containsExactlyInAnyOrderElementsOf(expectedOutParams);
        assertThat(inParams).isEmpty();

        nextTask.setOutParameters(outParams.stream());
        nextTask.setInParameters(inParams.stream());

        nextTask = processService.nextTask(pi.getId(), nextTask , true);

        outParams = nextTask.getOutParameters().collect(Collectors.toList());
        inParams = nextTask.getInParameters().collect(Collectors.toList());

        TaskModel task = nextTask;

        assertThat(nextTask.getId()).isNotNull();
        assertThat(nextTask.getMilestone()).isEqualTo("Referência Multibanco");
        assertThat(outParams).containsExactlyInAnyOrderElementsOf(expectedOutParams2);
        assertThat(inParams).isEmpty();

        nextTask.setOutParameters(outParams.stream());
        nextTask.setInParameters(inParams.stream());

        nextTask = processService.nextTask(pi.getId(), nextTask , true);

        assertThat(nextTask).isNull();

        assertThatExceptionOfType(DataNotFoundException.class)
                .isThrownBy(
                        () -> processService.nextTask(pi.getId(), task , true)
                );
    }

    @Test
    public void startProcessInstanceInvalidParams () throws BasicException{
        ProcessModel proc = new ProcessModel("Process_08rb4ct",Stream.empty());

        assertThatExceptionOfType(InvalidParametersException.class)
                .isThrownBy(
                        () -> processService.startProcessInstance(proc)
                );
    }

    @Before
    public void setUp() {
        ProcessMilestones pms = new ProcessMilestones();

        pms.setProcDefKey("Process_08rb4ct");
        pms.setMilestones(new String [] {"3","4"});

        Mockito.when(processRepository.findById(pms.getProcDefKey()))
                .thenReturn(Optional.of(pms));

        List<Milestones> milestones = new ArrayList<>();
        Milestones m1 = new Milestones();
        m1.setMilestoneId("3");
        m1.setMilestoneName("Movimentos");
        m1.setTasks(new String [] {"Task_1wayfyf"});
        Milestones m2 = new Milestones();
        m2.setMilestoneId("4");
        m2.setMilestoneName("Referência Multibanco");
        m2.setTasks(new String [] {"Task_19epn2w"});

        milestones.add(m1);
        milestones.add(m2);

        List<String> milestoneIds = Arrays.asList(pms.getMilestones());
        Mockito.when(milestoneRepository.findAllById(milestoneIds))
                .thenReturn(milestones);

        Mockito.when(milestoneRepository.getMilestoneOfTask("Task_0woay55", pms.getProcDefKey()))
                .thenReturn(null);

        Mockito.when(milestoneRepository.getMilestoneOfTask("Task_1wayfyf", pms.getProcDefKey()))
                .thenReturn(m1.getMilestoneName());

        Mockito.when(milestoneRepository.getMilestoneOfTask("Task_19epn2w", pms.getProcDefKey()))
                .thenReturn(m2.getMilestoneName());
    }

    private List<ParameterModel> firstTaskOutParams() {
        List<ParameterModel> expectedOutParams =new ArrayList<>();

        List<MovementDTO> movements = new ArrayList<>();
        movements.add(new MovementDTO("32.97","104.82","FT 201802/624373","2018-01-03T00:00:00Z",
                "NÃ£o regularizado","32.97", new InvoiceDTO("284452536")));
        movements.add(new MovementDTO("32.97","71.85","FT 201802/624266","2017-12-04T00:00:00Z",
                "NÃ£o regularizado","32.97", new InvoiceDTO("284452169")));
        movements.add(new MovementDTO("32.97","38.88","FT 201802/624251","2017-11-03T00:00:00Z",
                "NÃ£o regularizado","32.97", new InvoiceDTO("284452127")));
        movements.add(new MovementDTO("5.91","5.91","FT 201802/624220","2017-10-03T23:00:00Z",
                "NÃ£o regularizado","5.91", new InvoiceDTO("284452087")));

        expectedOutParams.add(new ParameterModel("movements",movements));

        return expectedOutParams;
    }

    private List<ParameterModel> secondTaskOutParams() {
        List<ParameterModel> expectedOutParams =new ArrayList<>();

        expectedOutParams.add(new ParameterModel("amount","37.94"));
        expectedOutParams.add(new ParameterModel("entity","11111"));
        expectedOutParams.add(new ParameterModel("reference","222222222222"));

        return expectedOutParams;
    }
}