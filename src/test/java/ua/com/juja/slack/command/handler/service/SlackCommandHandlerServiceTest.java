package ua.com.juja.slack.command.handler.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ua.com.juja.slack.command.handler.UserBySlackName;
import ua.com.juja.slack.command.handler.model.SlackParsedCommand;
import ua.com.juja.slack.command.handler.model.UserData;

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
    private UserBySlackName userBySlackName;
    @Captor
    ArgumentCaptor<List<String>> captor;

    private SlackCommandHandlerService slackCommandHandlerService;

    private UserData userFrom;
    private UserData user1;
    private UserData user2;
    private UserData user3;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        userFrom = new UserData("AAA000", "UFDR97JLA");
        user1 = new UserData("AAA111", "U1DR97JLA");
        user2 = new UserData("AAA222", "U2DR97JLA");
        user3 = new UserData("AAA333", "U3DR97JLA");
        slackCommandHandlerService = new SlackCommandHandlerService(userBySlackName);
    }

    @Test
    public void getSlackParsedCommandOneSlackInText() throws Exception {
        //given
        final String text = "text <@U1DR97JLA|slackName1> TexT text.";
        final List<UserData> responseFromUserService = Arrays.asList(userFrom, user1);
        final SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, Collections.singletonList(user1));

        when(userBySlackName.findUsersBySlackUserId(anyListOf(String.class))).thenReturn(responseFromUserService);

        //when
        SlackParsedCommand actual = slackCommandHandlerService.createSlackParsedCommand(userFrom.getSlackUserId(), text);
        //then
        verify(userBySlackName, times(1)).findUsersBySlackUserId(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("U1DR97JLA", "UFDR97JLA"));
        verifyNoMoreInteractions(userBySlackName);
        assertEquals(expected, actual);
    }

    @Test
    public void getSlackParsedCommandThreeSlackInText() throws Exception {
        //given
        final String text = "text <@U1DR97JLA|slackName1> TexT <@U2DR97JLA|slackName2> text. <@U3DR97JLA|slackName3>";
        final List<UserData> responseFromUserService = Arrays.asList(userFrom, user1, user2, user3);
        final SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, Arrays.asList(user1, user2, user3));

        when(userBySlackName.findUsersBySlackUserId(anyListOf(String.class))).thenReturn(responseFromUserService);
        //when
        SlackParsedCommand actual = slackCommandHandlerService.createSlackParsedCommand(userFrom.getSlackUserId(), text);
        //then
        verify(userBySlackName, times(1)).findUsersBySlackUserId(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("U2DR97JLA", "U1DR97JLA", "UFDR97JLA", "U3DR97JLA"));
        verifyNoMoreInteractions(userBySlackName);
        assertEquals(expected, actual);
    }

    @Test
    public void getSlackParsedCommandTwoSlackInText() throws Exception {
        //given
        final String text = "text <@U1DR97JLA|slackName1> TexT <@U2DR97JLA|slackName2> text.";
        final List<UserData> responseFromUserService = Arrays.asList(userFrom, user2, user1);
        final SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, Arrays.asList(user1, user2));

        when(userBySlackName.findUsersBySlackUserId(anyListOf(String.class))).thenReturn(responseFromUserService);
        //when
        SlackParsedCommand actual = slackCommandHandlerService.createSlackParsedCommand(userFrom.getSlackUserId(), text);
        //then
        verify(userBySlackName, times(1)).findUsersBySlackUserId(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("U2DR97JLA", "U1DR97JLA", "UFDR97JLA"));
        verifyNoMoreInteractions(userBySlackName);
        assertEquals(expected, actual);
    }

    @Test
    public void getSlackParsedCommandWithoutSlackInText() throws Exception {
        //given
        final String text = "text without slack name TexT text.";
        final List<UserData> responseFromUserService = Collections.singletonList(userFrom);
        final SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, new ArrayList<>());

        when(userBySlackName.findUsersBySlackUserId(anyListOf(String.class))).thenReturn(responseFromUserService);
        //when
        SlackParsedCommand actual = slackCommandHandlerService.createSlackParsedCommand(userFrom.getSlackUserId(), text);
        //then
        verify(userBySlackName).findUsersBySlackUserId(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("UFDR97JLA"));
        verifyNoMoreInteractions(userBySlackName);
        assertEquals(expected, actual);
    }

    @Test
    public void getSlackParsedCommandIfTextContainsFromUserSlackName() throws Exception {
        //given
        final String text = "text <@U1DR97JLA|slackName1> TexT text. <@UFDR97JLA|fromSlackName>";
        final List<UserData> responseFromUserService = Arrays.asList(userFrom, user1);
        final SlackParsedCommand expected = new SlackParsedCommand(userFrom, text, Arrays.asList(user1, userFrom));

        when(userBySlackName.findUsersBySlackUserId(anyListOf(String.class))).thenReturn(responseFromUserService);

        //when
        SlackParsedCommand actual = slackCommandHandlerService.createSlackParsedCommand(userFrom.getSlackUserId(), text);
        //then
        verify(userBySlackName, times(1)).findUsersBySlackUserId(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder("U1DR97JLA", "UFDR97JLA"));
        verifyNoMoreInteractions(userBySlackName);
        assertEquals(expected, actual);
    }
}