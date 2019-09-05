import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.*;
import java.util.function.Supplier;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.demo.CamundaApplication;
import org.camunda.demo.ProcessDefinition;
import org.camunda.demo.dto.*;
import org.camunda.demo.exception.exceptions.CamundaException;
import org.camunda.demo.exception.exceptions.DataNotFoundException;
import org.camunda.demo.exception.exceptions.InvalidParametersException;
import org.camunda.demo.formatters.ProcessFormatter;
import org.camunda.demo.repo.ProcessRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= CamundaApplication.class)
@AutoConfigureMockMvc
@Transactional
@Rollback(true)
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:beforeTestRun.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class IntegrationTests {
	
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProcessRepository processRepository;

	@Autowired
    private RuntimeService runtimeService;
	
	@Autowired
    private TaskService taskService;
	
	@Autowired
	private ProcessFormatter processFormatter;
	
	@Test
	public void startProcInstanceSuccess() throws Exception{
		ProcessDTO processDTO = new ProcessDTO();
		processDTO.setProcDefKey("Process_05r67sz");
		
		ObjectMapper mapper = new ObjectMapper();
		String asString = mapper.writeValueAsString(processDTO);
		
		MvcResult result = mockMvc.perform(
				post("/camunda/process").contentType(MediaType.APPLICATION_JSON).content(asString)
		).andExpect(status().isOk()).andReturn();
		
		String contentAsString = result.getResponse().getContentAsString();
		ProcessDTO proc = mapper.readValue(contentAsString, ProcessDTO.class);
		
		List<String> expectMilestones = Arrays.asList("Input","Show Result");
		
		assertThat(proc.getProcInstId()).isNotNull();
		assertThat(proc.getProcDefKey()).isEqualTo("Process_05r67sz");
		assertThat(proc.getMilestones()).containsExactlyInAnyOrderElementsOf(expectMilestones);
		assertThat(proc.getVariables()).isEmpty();
		
		
		TaskDTO activeTask = proc.getActiveTask();
		
		List<ParameterDTO> expectInParams = new ArrayList<>();
		expectInParams.add(new ParameterDTO("n1",null));
		expectInParams.add(new ParameterDTO("n2",null));
		expectInParams.add(new ParameterDTO("toSum",null));
		
		assertThat(activeTask.getId()).isNotNull();
		assertThat(activeTask.getInParameters()).containsExactlyInAnyOrderElementsOf(expectInParams);
		assertThat(activeTask.getOutParameters()).isEmpty();
		assertThat(activeTask.getMilestone()).isEqualTo("Input");
	}
	
	@Test
	public void nextTaskSucess() throws Exception {
		
		Map<String, Object> variables = new HashMap<>();
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("Process_05r67sz",variables);
		Task bpmTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
		
		TaskDTO taskDTO = new TaskDTO();
		taskDTO.setId(bpmTask.getId());
		
		ArrayList<ParameterDTO> inParams = new ArrayList<>();
		inParams.add(new ParameterDTO("n1",3));
		inParams.add(new ParameterDTO("n2",4));
		inParams.add(new ParameterDTO("toSum",true));
		taskDTO.setInParameters(inParams);
		
		ObjectMapper mapper = new ObjectMapper();
		String asString = mapper.writeValueAsString(taskDTO);
		
		MvcResult result = mockMvc.perform(
				post("/camunda/process/{processId}/next",pi.getId())
					.param("advance", "true")
					.contentType(MediaType.APPLICATION_JSON)
					.content(asString)
		).andExpect(status().isOk()).andReturn();
	
		String contentAsString = result.getResponse().getContentAsString();
		TaskDTO task = mapper.readValue(contentAsString, TaskDTO.class);
		
		assertThat(task.getId()).isNotNull();
		assertThat(task.getMilestone()).isEqualTo("Show Result");
		assertThat(task.getInParameters()).isEmpty();
		
		List<ParameterDTO> expectOutParams = new ArrayList<>();
		expectOutParams.add(new ParameterDTO("n1",3));
		expectOutParams.add(new ParameterDTO("n2",4));
		expectOutParams.add(new ParameterDTO("toSum",true));
		expectOutParams.add(new ParameterDTO("result",7));
		
		assertThat(task.getOutParameters()).containsExactlyInAnyOrderElementsOf(expectOutParams);
		
		taskDTO.setId(task.getId());
		asString = mapper.writeValueAsString(taskDTO);
		
		result = mockMvc.perform(
				post("/camunda/process/{processId}/next",pi.getId())
					.param("advance", "true")
					.contentType(MediaType.APPLICATION_JSON)
					.content(asString)
		).andExpect(status().isOk()).andReturn();
		
		contentAsString = result.getResponse().getContentAsString();
		task = mapper.readValue(contentAsString, TaskDTO.class);
		
		assertThat(task.getId()).isNull();
		assertThat(task.getMilestone()).isNull();
		assertThat(task.getInParameters()).isNull();
		assertThat(task.getOutParameters()).isNull();
		
		asString = mapper.writeValueAsString(taskDTO);
		
		result = mockMvc.perform(
				post("/camunda/process/{processId}/next",pi.getId())
					.param("advance", "true")
					.contentType(MediaType.APPLICATION_JSON)
					.content(asString)
		).andExpect(status().isBadRequest()).andReturn();
		
		assertThat(result.getResolvedException()).isInstanceOf(DataNotFoundException.class);
	}


	@Test
	public void getTaskSucess() throws Exception {
		Map<String, Object> variables = new HashMap<>();
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("Process_05r67sz",variables);
		
		ObjectMapper mapper = new ObjectMapper();
		
		MvcResult result = mockMvc.perform(
				get("/camunda/process/{processId}/task",pi.getId())
		).andExpect(status().isOk()).andReturn();
		
		String contentAsString = result.getResponse().getContentAsString();
		TaskDTO task = mapper.readValue(contentAsString, TaskDTO.class);
		
		assertThat(task.getId()).isNotNull();
		assertThat(task.getMilestone()).isEqualTo("Input");
		assertThat(task.getOutParameters()).isEmpty();
		
		List<ParameterDTO> expectInParams = new ArrayList<>();
		expectInParams.add(new ParameterDTO("n1",null));
		expectInParams.add(new ParameterDTO("n2",null));
		expectInParams.add(new ParameterDTO("toSum",null));
		
		assertThat(task.getInParameters()).containsExactlyInAnyOrderElementsOf(expectInParams);
	}
	
	@Test
	public void getTaskBadProcessId() throws Exception {
		
		MvcResult result = mockMvc.perform(
				get("/camunda/process/{processId}/task",1234)
		).andExpect(status().isBadRequest()).andReturn();
		
		assertThat(result.getResolvedException()).isInstanceOf(DataNotFoundException.class);
	}
	
	@Test
	public void cancelProcInstanceSuccess () throws Exception{
		Map<String, Object> variables = new HashMap<>();
		String procId = runtimeService.startProcessInstanceByKey("Process_05r67sz",variables).getId();
		
		Supplier<ProcessInstance> pi = () -> runtimeService.createProcessInstanceQuery().processInstanceId(procId).singleResult();
		
		assertThat(pi.get()).isNotNull();
		
		mockMvc.perform(
				get("/camunda/process/{processId}/cancel",procId)
		).andExpect(status().isOk());
		
		assertThat(pi.get()).isNull();	
	}
	
	@Test
	public void cancelNonExistingProcInstance() throws Exception{
		
		MvcResult result = mockMvc.perform(
				get("/camunda/process/{processId}/cancel",123)
		).andExpect(status().isBadRequest()).andReturn();
		
		
		assertThat(result.getResolvedException()).isInstanceOf(DataNotFoundException.class);
	}
	
	@Test
	public void cancelAlreadyCanceledProcInstance() throws Exception{
		
		Map<String, Object> variables = new HashMap<>();
		String procId = runtimeService.startProcessInstanceByKey("Process_05r67sz",variables).getId();
		
		Supplier<ProcessInstance> pi = () -> runtimeService.createProcessInstanceQuery().processInstanceId(procId).singleResult();
		
		assertThat(pi.get()).isNotNull();
		
		mockMvc.perform(
				get("/camunda/process/{processId}/cancel",procId)
		).andExpect(status().isOk()).andReturn();
		
		assertThat(pi.get()).isNull();
		
		MvcResult result = mockMvc.perform(
				get("/camunda/process/{processId}/cancel",procId)
		).andExpect(status().isBadRequest()).andReturn();
		
		assertThat(result.getResolvedException()).isInstanceOf(DataNotFoundException.class);
	}
	
	@Test
	public void procHistorySucess() throws Exception{
		try {
			//Told that this process need an input variable named "user"
			processFormatter.formatter.get("Process_05r67sz").setInVariables(singletonList("user"));
			
			//Create two process instances and call end-point with no filter - should return the two instances
			Map<String, Object> p1vars = new HashMap<>();
			p1vars.put("user", "999");		
			runtimeService.startProcessInstanceByKey("Process_05r67sz",p1vars);
			
			Map<String, Object> p2vars = new HashMap<>();
			p2vars.put("user", "111");		
			runtimeService.startProcessInstanceByKey("Process_05r67sz",p2vars);
			
			MvcResult result = mockMvc.perform(
					get("/camunda/process/{procDefKey}/history","Process_05r67sz")
			).andExpect(status().isOk()).andReturn();
			
			ObjectMapper mapper = new ObjectMapper();
			String contentAsString = result.getResponse().getContentAsString();
			List<ProcessDTO> procs = mapper.readValue(contentAsString, new TypeReference<List<ProcessDTO>>(){});
			
			assertThat(procs).hasSize(2);
			
			List<String> expectMilestones = Arrays.asList("Input","Show Result");
			ProcessDTO processDTO = procs.get(0);
			
			assertThat(processDTO.getProcInstId()).isNotNull();
			assertThat(processDTO.getProcDefKey()).isEqualTo("Process_05r67sz");
			assertThat(processDTO.getMilestones()).containsExactlyInAnyOrderElementsOf(expectMilestones);
			assertThat(processDTO.getVariables()).containsExactlyInAnyOrderElementsOf(singletonList(new ParameterDTO("user", "111")));
			assertThat(processDTO.getState()).isEqualTo("ACTIVE");
			assertThat(processDTO.getStartDate()).isNotNull();
			assertThat(processDTO.getEndDate()).isNull();
			
			TaskDTO taskDTO = processDTO.getActiveTask();
			assertThat(taskDTO.getId()).isNotNull();
			assertThat(taskDTO.getMilestone()).isEqualTo("Input");
			assertThat(taskDTO.getOutParameters()).isEmpty();
			
			List<ParameterDTO> expectInParams = new ArrayList<>();
			expectInParams.add(new ParameterDTO("n1",null));
			expectInParams.add(new ParameterDTO("n2",null));
			expectInParams.add(new ParameterDTO("toSum",null));
			
			assertThat(taskDTO.getInParameters()).containsExactlyInAnyOrderElementsOf(expectInParams);
			
			//Call endpoint with a valid filter - should return the single process instance with user 999
			result = mockMvc.perform(
					get("/camunda/process/{procDefKey}/history","Process_05r67sz")
					.param("filterName", "user")
					.param("filterValue","999")
			).andExpect(status().isOk()).andReturn();
			
			contentAsString = result.getResponse().getContentAsString();
			procs = mapper.readValue(contentAsString, new TypeReference<List<ProcessDTO>>(){});
			
			assertThat(procs).hasSize(1);
			
			processDTO = procs.get(0);
			
			assertThat(processDTO.getProcInstId()).isNotNull();
			assertThat(processDTO.getProcDefKey()).isEqualTo("Process_05r67sz");
			assertThat(processDTO.getMilestones()).containsExactlyInAnyOrderElementsOf(expectMilestones);
			assertThat(processDTO.getVariables()).containsExactlyInAnyOrderElementsOf(singletonList(new ParameterDTO("user", "999")));
			assertThat(processDTO.getState()).isEqualTo("ACTIVE");
			assertThat(processDTO.getStartDate()).isNotNull();
			assertThat(processDTO.getEndDate()).isNull();
			
			taskDTO = processDTO.getActiveTask();
			assertThat(taskDTO.getId()).isNotNull();
			assertThat(taskDTO.getMilestone()).isEqualTo("Input");
			assertThat(taskDTO.getOutParameters()).isEmpty();
			assertThat(taskDTO.getInParameters()).containsExactlyInAnyOrderElementsOf(expectInParams);
			
			//Cancel a process instance and call the endpoint again - should return a processDTO with state terminated, endDate and no task
			runtimeService.deleteProcessInstance(processDTO.getProcInstId(),null);
			
			result = mockMvc.perform(
					get("/camunda/process/{procDefKey}/history","Process_05r67sz")
					.param("filterName", "user")
					.param("filterValue","999")
			).andExpect(status().isOk()).andReturn();
			
			contentAsString = result.getResponse().getContentAsString();
			procs = mapper.readValue(contentAsString, new TypeReference<List<ProcessDTO>>(){});
			
			assertThat(procs).hasSize(1);
			
			processDTO = procs.get(0);
			
			assertThat(processDTO.getProcInstId()).isNotNull();
			assertThat(processDTO.getProcDefKey()).isEqualTo("Process_05r67sz");
			assertThat(processDTO.getMilestones()).containsExactlyInAnyOrderElementsOf(expectMilestones);
			assertThat(processDTO.getVariables()).containsExactlyInAnyOrderElementsOf(singletonList(new ParameterDTO("user", "999")));
			assertThat(processDTO.getState()).isEqualTo("INTERNALLY_TERMINATED");
			assertThat(processDTO.getStartDate()).isNotNull();
			assertThat(processDTO.getEndDate()).isNotNull();
			
			assertThat(processDTO.getActiveTask()).isNull();
			
			//Call endpoint with a non valid filter - should return an empty list
			result = mockMvc.perform(
					get("/camunda/process/{procDefKey}/history","Process_05r67sz")
					.param("filterName", "client")
					.param("filterValue","999")
			).andExpect(status().isOk()).andReturn();
			
			contentAsString = result.getResponse().getContentAsString();
			procs = mapper.readValue(contentAsString, new TypeReference<List<ProcessDTO>>(){});
			
			assertThat(procs).isEmpty();
		}finally {
			//Restore process input variables to none 
			processFormatter.formatter.get("Process_05r67sz").setInVariables(emptyList());
		}
	}
	
	@Test
	public void startProcInstanceInvalidParams() throws Exception{
		try {
			//Need a variable user that is not passed
			processFormatter.formatter.get("Process_05r67sz").setInVariables(singletonList("user"));
			
			ProcessDTO processDTO = new ProcessDTO();
			processDTO.setProcDefKey("Process_05r67sz");
			
			ObjectMapper mapper = new ObjectMapper();
			String asString = mapper.writeValueAsString(processDTO);
			
			MvcResult result = mockMvc.perform(
					post("/camunda/process").contentType(MediaType.APPLICATION_JSON).content(asString)
			).andExpect(status().isBadRequest()).andReturn();
			
			assertThat(result.getResolvedException()).isInstanceOf(InvalidParametersException.class);
		}finally{
			processFormatter.formatter.get("Process_05r67sz").setInVariables(emptyList());
		}
	}
	
	@Test
	public void startProcInstanceInvalidProcDefKey() throws Exception{
		ProcessDTO processDTO = new ProcessDTO();

		ObjectMapper mapper = new ObjectMapper();
		String asString = mapper.writeValueAsString(processDTO);
			
		MvcResult result = mockMvc.perform(
				post("/camunda/process").contentType(MediaType.APPLICATION_JSON).content(asString)
		).andExpect(status().isBadRequest()).andReturn();
			
		assertThat(result.getResolvedException()).isInstanceOf(InvalidParametersException.class);

        processDTO.setProcDefKey("Process_1212");
        asString = mapper.writeValueAsString(processDTO);

        result = mockMvc.perform(
                post("/camunda/process").contentType(MediaType.APPLICATION_JSON).content(asString)
        ).andExpect(status().isBadRequest()).andReturn();

        assertThat(result.getResolvedException()).isInstanceOf(InvalidParametersException.class);
    }

	@Test
	public void getProcessInfoSucess() throws Exception{
		Map<String, Object> variables = new HashMap<>();
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("Process_05r67sz",variables);
		
		ObjectMapper mapper = new ObjectMapper();
		
		MvcResult result = mockMvc.perform(
				get("/camunda/process/{procInstId}/info",pi.getId())
		).andExpect(status().isOk()).andReturn();
		
		String contentAsString = result.getResponse().getContentAsString();
		ProcessDTO proc = mapper.readValue(contentAsString, ProcessDTO.class);
		
		List<String> expectMilestones = Arrays.asList("Input","Show Result");
		
		assertThat(proc.getProcInstId()).isNotNull();
		assertThat(proc.getProcDefKey()).isEqualTo("Process_05r67sz");
		assertThat(proc.getMilestones()).containsExactlyInAnyOrderElementsOf(expectMilestones);
		assertThat(proc.getVariables()).isEmpty();
		
		
		TaskDTO activeTask = proc.getActiveTask();
		
		List<ParameterDTO> expectInParams = new ArrayList<>();
		expectInParams.add(new ParameterDTO("n1",null));
		expectInParams.add(new ParameterDTO("n2",null));
		expectInParams.add(new ParameterDTO("toSum",null));
		
		assertThat(activeTask.getId()).isNotNull();
		assertThat(activeTask.getInParameters()).containsExactlyInAnyOrderElementsOf(expectInParams);
		assertThat(activeTask.getOutParameters()).isEmpty();
		assertThat(activeTask.getMilestone()).isEqualTo("Input");
	}

	@Test
	public void procWithoutMilestones() throws Exception{
		processRepository.deleteById("Process_05r67sz");

		ProcessDTO processDTO = new ProcessDTO();
		processDTO.setProcDefKey("Process_05r67sz");

		ObjectMapper mapper = new ObjectMapper();
		String asString = mapper.writeValueAsString(processDTO);

		MvcResult result = mockMvc.perform(
				post("/camunda/process").contentType(MediaType.APPLICATION_JSON).content(asString)
		).andExpect(status().isOk()).andReturn();

		String contentAsString = result.getResponse().getContentAsString();
		ProcessDTO proc = mapper.readValue(contentAsString, ProcessDTO.class);

		assertThat(proc.getMilestones()).isEmpty();
		assertThat(proc.getProcDefKey()).isEqualTo("Process_05r67sz");
		assertThat(proc.getActiveTask()).isNotNull();
		assertThat(proc.getProcInstId()).isNotNull();

		String procInstId = proc.getProcInstId();
		result = mockMvc.perform(
				get("/camunda/process/{procDefKey}/history", "Process_05r67sz")
		).andExpect(status().isOk()).andReturn();

		contentAsString = result.getResponse().getContentAsString();
		List<ProcessDTO> procs = mapper.readValue(contentAsString, new TypeReference<List<ProcessDTO>>(){});
		proc= procs.get(0);

		assertThat(proc.getMilestones()).isEmpty();
		assertThat(proc.getState()).isEqualTo("ACTIVE");
		assertThat(proc.getProcDefKey()).isEqualTo("Process_05r67sz");
		assertThat(proc.getActiveTask()).isNotNull();
		assertThat(proc.getProcInstId()).isNotNull();
		assertThat(proc.getStartDate()).isNotNull();
		assertThat(proc.getEndDate()).isNull();

		result = mockMvc.perform(
				get("/camunda/process/{procInstId}/info",procInstId)
		).andExpect(status().isOk()).andReturn();

		contentAsString = result.getResponse().getContentAsString();
		proc = mapper.readValue(contentAsString, ProcessDTO.class);

		assertThat(proc.getMilestones()).isEmpty();
		assertThat(proc.getState()).isEqualTo("ACTIVE");
		assertThat(proc.getProcDefKey()).isEqualTo("Process_05r67sz");
		assertThat(proc.getActiveTask()).isNotNull();
		assertThat(proc.getProcInstId()).isNotNull();
		assertThat(proc.getStartDate()).isNotNull();
		assertThat(proc.getEndDate()).isNull();
	}

    @Test
    public void startProcInstanceNoDeployedProcDefKey() throws Exception{
	    try {
	        processFormatter.formatter.put("Process_1212", new ProcessDefinition(emptyList(),emptyList()));

            ProcessDTO processDTO = new ProcessDTO();
            processDTO.setProcDefKey("Process_1212");

            ObjectMapper mapper = new ObjectMapper();
            String asString = mapper.writeValueAsString(processDTO);

            MvcResult result = mockMvc.perform(
                    post("/camunda/process").contentType(MediaType.APPLICATION_JSON).content(asString)
            ).andExpect(status().isBadRequest()).andReturn();

            assertThat(result.getResolvedException()).isInstanceOf(CamundaException.class);

        }finally {
            processFormatter.formatter.remove("Process_1212");
        }
	}

    @Test
    public void getProcHistoryInvalidProcDefKey() throws Exception{
        MvcResult result = mockMvc.perform(
                get("/camunda/process/{procDefKey}/history", "Process_123")
        ).andExpect(status().isBadRequest()).andReturn();

        assertThat(result.getResolvedException()).isInstanceOf(InvalidParametersException.class);
    }

    @Test
    public void getProcInfoInvalidProcId() throws Exception{
        MvcResult result = mockMvc.perform(
                get("/camunda/process/{procInstId}/info",123)
        ).andExpect(status().isBadRequest()).andReturn();

        assertThat(result.getResolvedException()).isInstanceOf(DataNotFoundException.class);
    }

    @Test
    public void getProcInfoInvalidProcDefKey() throws Exception{
	    try {
	        processFormatter.formatter.remove("Process_05r67sz");

            ProcessInstance pi = runtimeService.startProcessInstanceByKey("Process_05r67sz", (Map<String, Object>) null);

            MvcResult result = mockMvc.perform(
                    get("/camunda/process/{procInstId}/info",pi.getId())
            ).andExpect(status().isBadRequest()).andReturn();

            assertThat(result.getResolvedException()).isInstanceOf(InvalidParametersException.class);

        }finally {
            processFormatter.formatter.put("Process_05r67sz",new ProcessDefinition(emptyList(),
                    Arrays.asList("n1","n2","toSum","result")));
        }
	}

    @Test
    public void fullTestProcess_08rb4ct() throws Exception{
        ProcessDTO processDTO = new ProcessDTO();
        processDTO.setProcDefKey("Process_08rb4ct");

        LinkedHashMap<Object, Object> clientDetails = new LinkedHashMap<>();
        clientDetails.put("nif","263884205");
        clientDetails.put("accountId","263884205");
        clientDetails.put("personId","112341198");

        processDTO.setVariables(
                Arrays.asList(
                        new ParameterDTO("client","263884205"),
                        new ParameterDTO("clientDetails", clientDetails),
                        new ParameterDTO("user","user123")
                )
        );

        ObjectMapper mapper = new ObjectMapper();
        String asString = mapper.writeValueAsString(processDTO);

        MvcResult result = mockMvc.perform(
                post("/camunda/process").contentType(MediaType.APPLICATION_JSON).content(asString)
        ).andExpect(status().isOk()).andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        ProcessDTO proc = mapper.readValue(contentAsString, ProcessDTO.class);

        List<String> expectMilestones = Arrays.asList("Movimentos","Referencia Multibanco");
        List<ParameterDTO> expectVars = Arrays.asList(
                new ParameterDTO("client","263884205"),
                new ParameterDTO("clientDetails", clientDetails),
                new ParameterDTO("user","user123")
        );

        assertThat(proc.getProcInstId()).isNotNull();
        assertThat(proc.getProcDefKey()).isEqualTo("Process_08rb4ct");
        assertThat(proc.getMilestones()).containsExactlyInAnyOrderElementsOf(expectMilestones);
        assertThat(proc.getVariables()).containsExactlyInAnyOrderElementsOf(expectVars);

        TaskDTO task = proc.getActiveTask();

        assertThat(task.getId()).isNotNull();
        assertThat(task.getMilestone()).isEqualTo("Movimentos");

        List<MovementDTO> expectedOutParams = Arrays.asList(
                new MovementDTO("32.97","104.82","FT 201802/624373","2018-01-03T00:00:00Z",
                        "NÃ£o regularizado","32.97", new InvoiceDTO("284452536")),
                new MovementDTO("32.97","71.85","FT 201802/624266","2017-12-04T00:00:00Z",
                        "NÃ£o regularizado","32.97", new InvoiceDTO("284452169")),
                new MovementDTO("32.97","38.88","FT 201802/624251","2017-11-03T00:00:00Z",
                        "NÃ£o regularizado","32.97", new InvoiceDTO("284452127")),
                new MovementDTO("5.91","5.91","FT 201802/624220","2017-10-03T23:00:00Z",
                        "NÃ£o regularizado","5.91", new InvoiceDTO("284452087"))
        );
        List<MovementDTO> outParams = mapper.convertValue(task.getOutParameters().get(0).getValue(),new TypeReference<List<MovementDTO>>(){} );

        assertThat(outParams).containsExactlyInAnyOrderElementsOf(expectedOutParams);
        assertThat(task.getInParameters()).isEmpty();

        asString = mapper.writeValueAsString(task);

        result = mockMvc.perform(
                post("/camunda/process/{processId}/next",proc.getProcInstId())
                        .param("advance", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asString)
        ).andExpect(status().isOk()).andReturn();

        contentAsString = result.getResponse().getContentAsString();
        task = mapper.readValue(contentAsString, TaskDTO.class);

        assertThat(task.getId()).isNotNull();
        assertThat(task.getMilestone()).isEqualTo("Referencia Multibanco");
        assertThat(task.getInParameters()).isEmpty();
        assertThat(task.getOutParameters()).containsExactlyInAnyOrderElementsOf(
                Arrays.asList(
                        new ParameterDTO("amount","37.94"),
                        new ParameterDTO("entity","11111"),
                        new ParameterDTO("reference","222222222222")
                )
        );

    }

	@Test
	public void fullTestProcess_02jaj7t() throws Exception{
		ProcessDTO processDTO = new ProcessDTO();
		processDTO.setProcDefKey("Process_02jaj7t");

		ObjectMapper mapper = new ObjectMapper();
		String asString = mapper.writeValueAsString(processDTO);

		MvcResult result = mockMvc.perform(
				post("/camunda/process").contentType(MediaType.APPLICATION_JSON).content(asString)
		).andExpect(status().isOk()).andReturn();

		String contentAsString = result.getResponse().getContentAsString();
		ProcessDTO proc = mapper.readValue(contentAsString, ProcessDTO.class);

		assertThat(proc.getProcInstId()).isNotNull();
		assertThat(proc.getProcDefKey()).isEqualTo("Process_02jaj7t");
		assertThat(proc.getMilestones()).isEmpty();
		assertThat(proc.getVariables()).isEmpty();

		TaskDTO task = proc.getActiveTask();

		List<ParameterDTO> inParams = Arrays.asList(
				new ParameterDTO("name", null),
				new ParameterDTO("birthdate", null),
				new ParameterDTO("gender", null),
				new ParameterDTO("notify", null),
				new ParameterDTO("email", null)
		);

		assertThat(task.getId()).isNotNull();
		assertThat(task.getMilestone()).isNull();
		assertThat(task.getInParameters()).containsExactlyInAnyOrderElementsOf(inParams);
		assertThat(task.getOutParameters()).isEmpty();


		task.setInParameters(
                Arrays.asList(
                        new ParameterDTO("name", "Tiago"),
                        new ParameterDTO("birthdate", "12/04/1997"),
                        new ParameterDTO("gender", "M"),
                        new ParameterDTO("notify", false),
                        new ParameterDTO("email", "t@mail.com")
                )
        );

        asString = mapper.writeValueAsString(task);

        result = mockMvc.perform(
                post("/camunda/process/{processId}/next",proc.getProcInstId())
                        .param("advance", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asString)
        ).andExpect(status().isOk()).andReturn();

        contentAsString = result.getResponse().getContentAsString();
        task = mapper.readValue(contentAsString, TaskDTO.class);

        assertThat(task.getId()).isNotNull();
        assertThat(task.getMilestone()).isNull();
        assertThat(task.getInParameters()).isEmpty();
        assertThat(task.getOutParameters()).containsExactlyInAnyOrderElementsOf(
                Arrays.asList(
                        new ParameterDTO("amount","37.94"),
                        new ParameterDTO("entity","11111"),
                        new ParameterDTO("reference","222222222222")
                )
        );

		result = mockMvc.perform(
				post("/camunda/process/{processId}/next",proc.getProcInstId())
						.param("advance", "true")
						.contentType(MediaType.APPLICATION_JSON)
						.content(asString)
		).andExpect(status().isOk()).andReturn();

		contentAsString = result.getResponse().getContentAsString();
		task = mapper.readValue(contentAsString, TaskDTO.class);

		assertThat(task.getId()).isNull();
	}

}