app.controller('usersController', function($scope) {
    $scope.headingTitle = "User List";
});

app.controller('rolesController', function($scope) {
    $scope.headingTitle = "Roles List";
});

app.controller('bootstrapController', ['$scope', function ($scope) {
    $scope.layout = function (gender) {
        $scope.result = gender;
    }
}]);
