package com.zoya.sudoku.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zoya.sudoku.AppContainer
import com.zoya.sudoku.ui.constructor.ConstructorScreen
import com.zoya.sudoku.ui.constructor.ConstructorTipsScreen
import com.zoya.sudoku.ui.constructor.ConstructorViewModel
import com.zoya.sudoku.ui.game.GameScreen
import com.zoya.sudoku.ui.game.GameViewModel
import com.zoya.sudoku.ui.home.HomeScreen
import com.zoya.sudoku.ui.home.HomeViewModel
import com.zoya.sudoku.ui.library.LibraryScreen
import com.zoya.sudoku.ui.library.LibraryViewModel
import com.zoya.sudoku.ui.stats.StatsScreen
import com.zoya.sudoku.ui.stats.StatsViewModel

/** Clears the whole back stack and makes Home the new root - reachable from any screen. */
private fun goHome(navController: NavController) {
    navController.navigate(Routes.HOME) {
        popUpTo(0)
        launchSingleTop = true
    }
}

@Composable
fun SudokuNavHost(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val vm = viewModel<HomeViewModel>(
                factory = viewModelFactory {
                    initializer { HomeViewModel(container.puzzleRepository, container.regionLayoutRepository) }
                }
            )
            HomeScreen(
                viewModel = vm,
                onConstructor = { navController.navigate(Routes.CONSTRUCTOR) },
                onLibrary = { navController.navigate(Routes.LIBRARY) },
                onStats = { navController.navigate(Routes.STATS) },
                onContinue = { navController.navigate(Routes.GAME) },
                onPlay = { navController.navigate(Routes.GAME) { popUpTo(Routes.HOME) } }
            )
        }
        composable(Routes.CONSTRUCTOR) {
            val vm = viewModel<ConstructorViewModel>(
                factory = viewModelFactory {
                    initializer { ConstructorViewModel(container.regionLayoutRepository) }
                }
            )
            ConstructorScreen(
                viewModel = vm,
                onSaved = { navController.navigate(Routes.LIBRARY) { popUpTo(Routes.HOME) } },
                onHome = { goHome(navController) },
                onTips = { navController.navigate(Routes.CONSTRUCTOR_TIPS) }
            )
        }
        composable(Routes.CONSTRUCTOR_TIPS) {
            // Back here means "return to the Constructor", not "abandon it and go Home" - the
            // in-progress coloring lives in ConstructorViewModel, scoped to that still-open entry.
            ConstructorTipsScreen(onHome = { navController.popBackStack() })
        }
        composable(Routes.LIBRARY) {
            val vm = viewModel<LibraryViewModel>(
                factory = viewModelFactory {
                    initializer { LibraryViewModel(container.regionLayoutRepository, container.puzzleRepository) }
                }
            )
            LibraryScreen(
                viewModel = vm,
                onPlay = { navController.navigate(Routes.GAME) { popUpTo(Routes.HOME) } },
                onHome = { goHome(navController) }
            )
        }
        composable(Routes.STATS) {
            val vm = viewModel<StatsViewModel>(
                factory = viewModelFactory {
                    initializer { StatsViewModel(container.statsRepository) }
                }
            )
            StatsScreen(viewModel = vm, onHome = { goHome(navController) })
        }
        composable(Routes.GAME) {
            val vm = viewModel<GameViewModel>(
                factory = viewModelFactory {
                    initializer {
                        GameViewModel(container.puzzleRepository, container.regionLayoutRepository, container.statsRepository)
                    }
                }
            )
            GameScreen(viewModel = vm, onHome = { goHome(navController) })
        }
    }
}
