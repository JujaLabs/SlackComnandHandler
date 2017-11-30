package ua.com.juja.slack.command.handler.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ua.com.juja.slack.command.handler.UserBySlackUserId;
import ua.com.juja.slack.command.handler.model.SlackParsedCommand;
import ua.com.juja.slack.command.handler.model.UserDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Nikolay Horushko
 * @author Dmitriy Lyashenko
 */
public class SlackCommandHandlerServiceTest {

    @Mock
    private UserBySlackUserId userBySlackUserId;
    @Captor
    private ArgumentCaptor<List<String>> captor;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private SlackCommandHandlerService slackCommandHandlerService;

    private UserDTO userFrom;
    private UserDTO user1;
    private UserDTO user2;
    private UserDTO user3;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        userFrom = new UserDTO("AAA000", "UFDR97JLA");
        user1 = new UserDTO("AAA111", "U1DR97JLA");
        user2 = new UserDTO("AAA222", "U2DR97JLA");
        user3 = new UserDTO("AAA333", "U3DR97JLA");
        slackCommandHandlerService = new SlackCommandHandlerService(userBySlackUserId);
    }

    @Test
    public void getSlackParsedCommandOneSlackInText() throws Exception {
        //given
        final String text = "text <@U1DR97JLA|slackName1> TexT text.";
        final List<UserDTO> responseFromUserService = Arrays.asList(userFrom, user1);
        final SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, Collections.singletonList(user1));

        when(userBySlackUserId.findUsersBySlackUserId(anyListOf(String.class))).thenReturn(responseFromUserService);

        //when
        SlackParsedCommand actual = slackCommandHandlerService.createSlackParsedCommand(userFrom.getSlackUserId(), text);
        //then
        verify(userBySlackUserId, times(1)).findUsersBySlackUserId(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("U1DR97JLA", "UFDR97JLA"));
        verifyNoMoreInteractions(userBySlackUserId);
        assertEquals(expected, actual);
    }

    @Test
    public void getSlackParsedCommandThreeSlackInText() throws Exception {
        //given
        final String text = "text <@U1DR97JLA|slackName1> TexT <@U2DR97JLA|slackName2> text. <@U3DR97JLA|slackName3>";
        final List<UserDTO> responseFromUserService = Arrays.asList(userFrom, user1, user2, user3);
        final SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, Arrays.asList(user1, user2, user3));

        when(userBySlackUserId.findUsersBySlackUserId(anyListOf(String.class))).thenReturn(responseFromUserService);
        //when
        SlackParsedCommand actual = slackCommandHandlerService.createSlackParsedCommand(userFrom.getSlackUserId(), text);
        //then
        verify(userBySlackUserId, times(1)).findUsersBySlackUserId(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("U2DR97JLA", "U1DR97JLA", "UFDR97JLA", "U3DR97JLA"));
        verifyNoMoreInteractions(userBySlackUserId);
        assertEquals(expected, actual);
    }

    @Test
    public void getSlackParsedCommandTwoSlackInText() throws Exception {
        //given
        final String text = "text <@U1DR97JLA|slackName1> TexT <@U2DR97JLA|slackName2> text.";
        final List<UserDTO> responseFromUserService = Arrays.asList(userFrom, user2, user1);
        final SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, Arrays.asList(user1, user2));

        when(userBySlackUserId.findUsersBySlackUserId(anyListOf(String.class))).thenReturn(responseFromUserService);
        //when
        SlackParsedCommand actual = slackCommandHandlerService.createSlackParsedCommand(userFrom.getSlackUserId(), text);
        //then
        verify(userBySlackUserId, times(1)).findUsersBySlackUserId(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("U2DR97JLA", "U1DR97JLA", "UFDR97JLA"));
        verifyNoMoreInteractions(userBySlackUserId);
        assertEquals(expected, actual);
    }

    @Test
    public void getSlackParsedCommandWithoutSlackInText() throws Exception {
        //given
        final String text = "text without slack name TexT text.";
        final List<UserDTO> responseFromUserService = Collections.singletonList(userFrom);
        final SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, new ArrayList<>());

        when(userBySlackUserId.findUsersBySlackUserId(anyListOf(String.class))).thenReturn(responseFromUserService);
        //when
        SlackParsedCommand actual = slackCommandHandlerService.createSlackParsedCommand(userFrom.getSlackUserId(), text);
        //then
        verify(userBySlackUserId).findUsersBySlackUserId(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("UFDR97JLA"));
        verifyNoMoreInteractions(userBySlackUserId);
        assertEquals(expected, actual);
    }

    @Test
    public void getSlackParsedCommandIfTextContainsFromUserSlackName() throws Exception {
        //given
        final String text = "text <@U1DR97JLA|slackName1> TexT text. <@UFDR97JLA|fromSlackName>";
        final List<UserDTO> responseFromUserService = Arrays.asList(userFrom, user1);
        final SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, Arrays.asList(user1, userFrom));

        when(userBySlackUserId.findUsersBySlackUserId(anyListOf(String.class))).thenReturn(responseFromUserService);

        //when
        SlackParsedCommand actual = slackCommandHandlerService.createSlackParsedCommand(userFrom.getSlackUserId(), text);
        //then
        verify(userBySlackUserId, times(1)).findUsersBySlackUserId(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("U1DR97JLA", "UFDR97JLA"));
        verifyNoMoreInteractions(userBySlackUserId);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldThrowExceptionIfReceiveWrongCountOfUsersFromUserService() throws Exception {
        //given
        final String text = "text <@U1DR97JLA|slackName1> TexT <@U2DR97JLA|slackName2> text.";
        final List<UserDTO> responseFromUserService = Arrays.asList(userFrom, user2);

        when(userBySlackUserId.findUsersBySlackUserId(anyListOf(String.class))).thenReturn(responseFromUserService);
        //then
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Sent [3] slackUsersId to UserService, but received [2] users");

        //when
        slackCommandHandlerService.createSlackParsedCommand(userFrom.getSlackUserId(), text);
    }

    @Test
    public void shouldThrowExceptionIfReceiveWrongUserFromUserService() throws Exception {
        //given
        final String text = "text <@U1DR97JLA|slackName1> TexT <@U2DR97JLA|slackName2> text.";
        final UserDTO wrongUser = new UserDTO("uuidW", "@UWDR97JLA");
        final List<UserDTO> responseFromUserService = Arrays.asList(userFrom, user1, wrongUser);

        when(userBySlackUserId.findUsersBySlackUserId(anyListOf(String.class))).thenReturn(responseFromUserService);
        //then
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Error. User for slackUserId: [U2DR97JLA] didn't find in the List of Users");

        //when
        slackCommandHandlerService.createSlackParsedCommand(userFrom.getSlackUserId(), text);
    }
}